package com.programmer74.katolk.server.controllers

import com.programmer74.katolk.server.entity.ClientBinaryMessage
import com.programmer74.katolk.server.entity.ClientBinaryMessageType
import com.programmer74.katolk.server.entity.UserEntity
import com.programmer74.katolk.server.repositories.DialogVault
import com.programmer74.katolk.server.repositories.UserVault
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import java.io.IOException
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class WebsocketHandler(val userVault: UserVault,
                       val dialogVault: DialogVault) : BinaryWebSocketHandler() {

  val sessions = CopyOnWriteArrayList<WebSocketSession>()

  val notifyQueue = LinkedBlockingQueue<UserEntity>()

  @Throws(InterruptedException::class, IOException::class)
  public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    if (message.payload.startsWith("AUTH")) {
      val username = message.payload.split(" ")[1]
      val password = message.payload.split(" ")[2]
      val user = userVault.repository.findByUsername(username)!!
      if (userVault.checkPasswordMatches(user, password)) {
        userVault.addOnlineUser(user, session)
        notifyAboutUserStateChange(user)
        session.sendMessage(TextMessage("AUTH_OK"))
      }
    }
  }

  fun notifyUserAboutNewMessage(user: UserEntity) {
    notifyQueue.add(user)
  }

  fun notifyAboutUserStateChange(user: UserEntity) {
    val dialogs = dialogVault.getDialogs(user)
    val participants = dialogs
        .map { dialogVault.getParticipants(it) }
        .flatten()
        .toSet()
    participants.forEach {
      System.err.println("Scheduling ${it.username} about it")
      sendUpdate(it)
    }
  }

  @Throws(Exception::class)
  override fun afterConnectionEstablished(session: WebSocketSession?) {
    //the messages will be broadcasted to all users.
    sessions.add(session!!)
  }

  @Throws(Exception::class)
  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    sessions.remove(session)
    val user = userVault.getOnlineUser(session)!!
    userVault.dropOnlineUser(session)

    if (userVault.getOnlineSession(user) == null) {
      user.lastOnline = Instant.now().toEpochMilli()
      user.online = false
      userVault.repository.save(user)
    }

    notifyAboutUserStateChange(user)
    handleTalkEndAbnormally(session, user)
  }

  override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
    val payload = message.payload.array()
    val user = userVault.getOnlineUser(session)!!
    val clientRequest = ClientBinaryMessage.fromBytes(payload, user)
    handleBinaryMessagingLogic(clientRequest, session, user)
  }

  @PostConstruct
  fun scheduleUpdaterThread() {
    val t = Thread {
      while (true) {
        try {
          val user = notifyQueue.poll(5, TimeUnit.SECONDS) ?: continue
          sendUpdate(user)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
    t.start()
  }

  fun sendUpdate(user: UserEntity) {
    userVault.getOnlineUsers()
        .filterValues { it.id == user.id }
        .forEach { s, u ->
          s.secureSendMessage(TextMessage("UPDATE"))
          System.err.println("Notified ${u.username} about dialogue update")
        }
  }

  private fun handleBinaryMessagingLogic(
      clientRequest: ClientBinaryMessage,
      session: WebSocketSession,
      sender: UserEntity) {
    when {
      clientRequest.type == ClientBinaryMessageType.PING_SERVER_REQUEST -> {
        val clientResponse = ClientBinaryMessage(ClientBinaryMessageType.PING_SERVER_RESPONSE,
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

  private fun handleCallRequest(clientRequest: ClientBinaryMessage,
                                session: WebSocketSession,
                                sender: UserEntity) {
    val requestedUser = userVault.getOnlineUser(clientRequest.intPayload())
    if ((requestedUser == null) ||
        (userVault.getOnlineSession(requestedUser) == null) ||
        (userVault.getTalk(
            userVault.getOnlineSession(requestedUser)!!
        ) != null)) {
      val clientResponse = ClientBinaryMessage(ClientBinaryMessageType.CALL_ERROR,
          clientRequest.payload, sender)
      session.secureSendMessage(clientResponse)
    } else {
      val forwardedRequest = ClientBinaryMessage(ClientBinaryMessageType.CALL_REQUEST,
          sender.id, sender)
      val forwardTo = userVault.getOnlineSession(requestedUser)!!
      forwardTo.secureSendMessage(forwardedRequest)
    }
  }

  private fun handleCallResponse(clientRequest: ClientBinaryMessage,
                                session: WebSocketSession,
                                sender: UserEntity) {
    val forwardingToUser = userVault.getOnlineUser(clientRequest.intPayload())
    if (forwardingToUser == null) {
      val clientResponse = ClientBinaryMessage(ClientBinaryMessageType.CALL_ERROR,
          clientRequest.payload, sender)
      session.secureSendMessage(clientResponse)
    } else {
      val forwardedRequest = ClientBinaryMessage(clientRequest.type,
          sender.id, sender)
      val forwardTo = userVault.getOnlineSession(forwardingToUser)!!
      forwardTo.secureSendMessage(forwardedRequest)
      if (clientRequest.type == ClientBinaryMessageType.CALL_RESPONSE_ALLOW) {
        val first = session
        val second = forwardTo
        userVault.addTalk(first, second)
        first.secureSendMessage(
            ClientBinaryMessage(
                ClientBinaryMessageType.CALL_BEGIN,
                userVault.getOnlineUser(second)!!.id,
                sender)
        )
        second.secureSendMessage(
            ClientBinaryMessage(
                ClientBinaryMessageType.CALL_BEGIN,
                userVault.getOnlineUser(first)!!.id,
                sender)
        )
      }
    }
  }

  private fun handleTalkEndAbnormally(session: WebSocketSession, sender: UserEntity) {
    val talk = userVault.getTalk(session) ?: return
    val notifyTo =
        if (talk.key == session) talk.value else talk.key
    val msg =
        ClientBinaryMessage(ClientBinaryMessageType.CALL_END_ABNORMAL, ByteArray(0), sender)
    notifyTo.secureSendMessage(msg)
    userVault.removeTalk(notifyTo)
  }

  private fun handleTalkEndNormally(session: WebSocketSession, sender: UserEntity) {
    val talk = userVault.getTalk(session) ?: return
    val notifyTo =
    if (talk.key == session) talk.value else talk.key
    val msg =
        ClientBinaryMessage(ClientBinaryMessageType.CALL_END, ByteArray(0), sender)
    session.secureSendMessage(msg)
    notifyTo.secureSendMessage(msg)
    userVault.removeTalk(notifyTo)
  }

  private fun handleForwarding(message: ClientBinaryMessage, session: WebSocketSession) {
    val talk = userVault.getTalk(session) ?: return
    val forwardTo =
        if (talk.key == session) talk.value else talk.key

    forwardTo.secureSendMessage(message)
  }
  
  private fun WebSocketSession.secureSendMessage(msg: WebSocketMessage<*>) {
    synchronized(this) {
      sendMessage(msg)
    }
  }

  private fun WebSocketSession.secureSendMessage(msg: ClientBinaryMessage) {
    secureSendMessage(BinaryMessage(msg.toBytes()))
  }
}