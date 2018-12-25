package com.programmer74.katolk.server.controllers

import com.programmer74.katolk.server.entity.ClientBinaryMessage
import com.programmer74.katolk.server.entity.ClientBinaryMessageType
import com.programmer74.katolk.server.entity.UserEntity
import com.programmer74.katolk.server.repositories.DialogVault
import com.programmer74.katolk.server.repositories.UserVault
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
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
    user.lastOnline = Instant.now().toEpochMilli()
    userVault.repository.save(user)
    notifyAboutUserStateChange(user)
  }

  override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
    val payload = message.payload.array()
    val user = userVault.getOnlineUser(session)!!
    val clientMessage = ClientBinaryMessage.fromBytes(payload, user)
    val onlineUsers = userVault.getOnlineUsers().keys
    if (clientMessage.type == ClientBinaryMessageType.PING_SERVER_REQUEST) {
      val response = ClientBinaryMessage(ClientBinaryMessageType.PING_SERVER_RESPONSE, clientMessage.payload, user)
      session.sendMessage(BinaryMessage(response.toBytes()))
    } else {
      onlineUsers.forEach {
        it.sendMessage(BinaryMessage(clientMessage.toBytes()))
      }
    }
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
          s.sendMessage(TextMessage("UPDATE"))
          System.err.println("Notified ${u.username} about dialogue update")
        }
  }
}