package com.programmer74.katolk.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.programmer74.katolk.dao.User
import com.programmer74.katolk.dto.ClientBinaryMessage
import com.programmer74.katolk.exception.BadRequestException
import com.programmer74.katolk.exception.NotFoundException
import com.programmer74.katolk.service.DialogueService
import com.programmer74.katolk.service.TalkService
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class WebsocketHandler(
  private val onlineUserService: TalkService,
  private val dialogueService: DialogueService,
  private val binaryMessageHandler: BinaryMessageHandler
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
    binaryMessageHandler.handleUserDisconnected(session, user)
  }

  override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
    val payload = message.payload.array()
    val user = onlineUserService.getOnlineUser(session) ?: error("should-never-happen")
    val clientRequest = ClientBinaryMessage.fromBytes(payload, user)
    binaryMessageHandler.handleBinaryMessagingLogic(clientRequest, session, user)
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
        .forEach { (s, _) ->
          s.secureSendMessage(TextMessage(mapper.writeValueAsString(notification.payload)))
          logger.warn { "Notification $notification sent" }
        }
  }

  companion object : KLogging()
}