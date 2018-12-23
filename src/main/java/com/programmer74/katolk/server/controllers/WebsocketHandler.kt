package com.programmer74.katolk.server.controllers

import com.programmer74.katolk.common.data.User
import com.programmer74.katolk.server.repositories.UserVault
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
class WebsocketHandler(val userVault: UserVault) : BinaryWebSocketHandler() {

  val sessions = CopyOnWriteArrayList<WebSocketSession>()

  val notifyQueue = LinkedBlockingQueue<User>()

  @Throws(InterruptedException::class, IOException::class)
  public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    if (message.payload.startsWith("AUTH")) {
      val username = message.payload.split(" ")[1]
      val password = message.payload.split(" ")[2]
      val user = userVault.repository.findByUsername(username)!!
      if (userVault.checkPasswordMatches(user, password)) {
        userVault.addOnlineUser(user, session)
      }
    }
  }

  fun notifyUserAboutNewMessage(user: User) {
    notifyQueue.add(user)
  }

  @Throws(Exception::class)
  override fun afterConnectionEstablished(session: WebSocketSession?) {
    //the messages will be broadcasted to all users.
    sessions.add(session!!)
  }

  @Throws(Exception::class)
  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    sessions.remove(session)
    userVault.dropOnlineUser(session)
  }

  override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
    super.handleBinaryMessage(session, message)
  }

  @PostConstruct
  fun scheduleUpdaterThread() {
    val t = Thread {
      while (true) {
        try {
          val user = notifyQueue.poll(5, TimeUnit.SECONDS) ?: continue
          userVault.getOnlineUsers()
              .filterValues { it.id == user.id }
              .forEach { s, u ->
                s.sendMessage(TextMessage("UPDATE"))
                System.err.println("Notified ${u.username} about dialogue update")
              }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
    t.start()
  }
}