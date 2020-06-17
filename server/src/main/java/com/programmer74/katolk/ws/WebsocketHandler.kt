package com.programmer74.katolk.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.programmer74.katolk.dao.User
import com.programmer74.katolk.exception.BadRequestException
import com.programmer74.katolk.exception.NotFoundException
import com.programmer74.katolk.service.DialogueService
import com.programmer74.katolk.service.OnlineUserService
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class WebsocketHandler(
  private val onlineUserService: OnlineUserService,
  private val dialogueService: DialogueService
) : BinaryWebSocketHandler() {

  val sessions = CopyOnWriteArrayList<WebSocketSession>()

  val notifyQueue = LinkedBlockingQueue<WebsocketNotification>()

  private val mapper = ObjectMapper().registerKotlinModule()

  @Throws(InterruptedException::class, IOException::class)
  public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    if (message.payload.startsWith("AUTH")) {
      val username = session.principal?.name ?: throw BadRequestException("principle is empty")
      val user =
          onlineUserService.getUser(username) ?: throw NotFoundException("no user $username found")
      onlineUserService.addOnlineUser(user, session)
      notifyAboutUserStateChange(user)
      session.sendMessage(TextMessage("AUTH_OK"))
    }
  }

  fun notifyUserAboutNewMessage(dialogueId: Long, userToBeNotified: User) {
    notifyQueue.add(
        WebsocketNotification(
            userToBeNotified,
            WebsocketNotificationPayload(
                NotificationType.NEW_MESSAGE,
                mapOf("dialogueID" to dialogueId))))
  }

  fun notifyAboutUserStateChange(userHavingStateChanged: User) {
    val dialogs = dialogueService.getDialogs(userHavingStateChanged)
    val participants = dialogs
        .map { dialogueService.getParticipants(it) }
        .flatten()
        .toSet()
    participants.forEach {
      logger.warn { "Notifying userHavingStateChanged $it about state change of user $userHavingStateChanged" }
      notifyQueue.add(
          WebsocketNotification(
              it,
              WebsocketNotificationPayload(
                  NotificationType.USER_STATE_CHANGED,
                  mapOf("userID" to userHavingStateChanged.safeId()))))
    }
  }

  @Throws(Exception::class)
  override fun afterConnectionEstablished(session: WebSocketSession) {
    //the messages will be broadcasted to all users.
    sessions.add(session)
  }

  @Throws(Exception::class)
  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    sessions.remove(session)
    val user = onlineUserService.getOnlineUser(session) ?: error("should-never-happen")
    onlineUserService.dropOnlineUser(session)

    if (onlineUserService.getOnlineSession(user) == null) {
      onlineUserService.setOfflineInDatabase(user)
    }

    notifyAboutUserStateChange(user)
    handleTalkEndAbnormally(session, user)
  }

  override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
    val payload = message.payload.array()
    val user = onlineUserService.getOnlineUser(session) ?: error("should-never-happen")
    val clientRequest = ClientBinaryMessage.fromBytes(payload, user)
    handleBinaryMessagingLogic(clientRequest, session, user)
  }

  @PostConstruct
  fun scheduleUpdaterThread() {
    val t = Thread {
      while (true) {
        try {
          val notification = notifyQueue.poll(5, TimeUnit.SECONDS) ?: continue
          sendUpdate(notification)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
    t.start()
  }

  fun sendUpdate(notification: WebsocketNotification) {
    onlineUserService.getOnlineUsers()
        .filterValues { it.id == notification.user.id }
        .forEach { (s, u) ->
          s.secureSendMessage(TextMessage(mapper.writeValueAsString(notification.payload)))
          logger.warn { "Notification $notification sent" }
        }
  }

  private fun handleBinaryMessagingLogic(
    clientRequest: ClientBinaryMessage,
    session: WebSocketSession,
    sender: User
  ) {
    when {
      clientRequest.type == ClientBinaryMessageType.PING_SERVER_REQUEST -> {
        val clientResponse = ClientBinaryMessage(
            ClientBinaryMessageType.PING_SERVER_RESPONSE,
            clientRequest.payload, sender)
        session.secureSendMessage(clientResponse)
      }
      clientRequest.type == ClientBinaryMessageType.CALL_REQUEST -> {
        handleCallRequest(clientRequest, session, sender)
      }
      clientRequest.type == ClientBinaryMessageType.CALL_RESPONSE_ALLOW ||
          clientRequest.type == ClientBinaryMessageType.CALL_RESPONSE_DENY -> {
        handleCallResponse(clientRequest, session, sender)
      }
      clientRequest.type == ClientBinaryMessageType.CALL_END -> {
        handleTalkEndNormally(session, sender)
      }
      else -> handleForwarding(clientRequest, session)
    }
  }

  //TODO: handle IDs being LONG, not INT
  private fun handleCallRequest(
    clientRequest: ClientBinaryMessage,
    session: WebSocketSession,
    sender: User
  ) {
    val requestedUser = onlineUserService.getOnlineUser(clientRequest.intPayload().toLong())
    if ((requestedUser == null) ||
        (onlineUserService.getOnlineSession(requestedUser) == null) ||
        (onlineUserService.getTalk(
            onlineUserService.getOnlineSession(requestedUser) ?: error("should-never-happen")
        ) != null)
    ) {
      val clientResponse = ClientBinaryMessage(
          ClientBinaryMessageType.CALL_ERROR,
          clientRequest.payload, sender)
      session.secureSendMessage(clientResponse)
    } else {
      val forwardedRequest = ClientBinaryMessage(
          ClientBinaryMessageType.CALL_REQUEST,
          sender.safeId().toInt(), sender)
      val forwardTo =
          onlineUserService.getOnlineSession(requestedUser) ?: error("should-never-happen")
      forwardTo.secureSendMessage(forwardedRequest)
    }
  }

  //TODO: handle IDs being LONG, not INT
  private fun handleCallResponse(
    clientRequest: ClientBinaryMessage,
    session: WebSocketSession,
    sender: User
  ) {
    val forwardingToUser = onlineUserService.getOnlineUser(clientRequest.intPayload().toLong())
    if (forwardingToUser == null) {
      val clientResponse = ClientBinaryMessage(
          ClientBinaryMessageType.CALL_ERROR,
          clientRequest.payload, sender)
      session.secureSendMessage(clientResponse)
    } else {
      val forwardedRequest = ClientBinaryMessage(
          clientRequest.type,
          sender.safeId().toInt(), sender)
      val forwardTo = onlineUserService.getOnlineSession(forwardingToUser)!!
      forwardTo.secureSendMessage(forwardedRequest)
      if (clientRequest.type == ClientBinaryMessageType.CALL_RESPONSE_ALLOW) {
        val first = session
        val second = forwardTo
        onlineUserService.addTalk(first, second)
        //TODO: fix "!!"
        first.secureSendMessage(
            ClientBinaryMessage(
                ClientBinaryMessageType.CALL_BEGIN,
                onlineUserService.getOnlineUser(second)!!.safeId().toInt(),
                sender)
        )
        second.secureSendMessage(
            ClientBinaryMessage(
                ClientBinaryMessageType.CALL_BEGIN,
                onlineUserService.getOnlineUser(first)!!.safeId().toInt(),
                sender)
        )
      }
    }
  }

  private fun handleTalkEndAbnormally(session: WebSocketSession, sender: User) {
    val talk = onlineUserService.getTalk(session) ?: return
    val notifyTo =
        if (talk.key == session) talk.value else talk.key
    val msg =
        ClientBinaryMessage(ClientBinaryMessageType.CALL_END_ABNORMAL, ByteArray(0), sender)
    notifyTo.secureSendMessage(msg)
    onlineUserService.removeTalk(notifyTo)
  }

  private fun handleTalkEndNormally(session: WebSocketSession, sender: User) {
    val talk = onlineUserService.getTalk(session) ?: return
    val notifyTo =
        if (talk.key == session) talk.value else talk.key
    val msg =
        ClientBinaryMessage(ClientBinaryMessageType.CALL_END, ByteArray(0), sender)
    session.secureSendMessage(msg)
    notifyTo.secureSendMessage(msg)
    onlineUserService.removeTalk(notifyTo)
  }

  private fun handleForwarding(message: ClientBinaryMessage, session: WebSocketSession) {
    val talk = onlineUserService.getTalk(session) ?: return
    val forwardTo =
        if (talk.key == session) talk.value else talk.key

    forwardTo.secureSendMessage(message)
  }

  private fun WebSocketSession.secureSendMessage(msg: WebSocketMessage<*>) {
    synchronized(this) {
      try {
        sendMessage(msg)
      } catch (e: Exception) {
        logger.error(e) { "Error in secureSendMessage" }
      }
    }
  }

  private fun WebSocketSession.secureSendMessage(msg: ClientBinaryMessage) {
    secureSendMessage(BinaryMessage(msg.toBytes()))
  }

  companion object : KLogging()
}