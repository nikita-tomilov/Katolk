package com.programmer74.katolk.service

import com.programmer74.katolk.dao.User
import com.programmer74.katolk.dto.OnlineUserStatus
import com.programmer74.katolk.repository.OnlineUserRepositorySingleton
import mu.KLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Service
class TalkService(
  private val userService: UserService
) {

  private val onlineUsers = ConcurrentHashMap<WebSocketSession, User>()

  private val channelForwarding = ConcurrentHashMap<WebSocketSession, WebSocketSession>()

  private val userStatus = ConcurrentHashMap<User, OnlineUserStatus>()

  fun setOnlineInDatabase(user: User) {
    logger.warn { "Invoked setOnline on user '$user'" }
    OnlineUserRepositorySingleton.get().addUser(user)
  }

  fun setOfflineInDatabase(user: User) {
    logger.warn { "Invoked setOffline on user '$user'" }
    OnlineUserRepositorySingleton.get().removeUser(user)
  }

  fun getUserStatus(user: User) = userStatus[user]

  fun setUserStatus(user: User?, status: OnlineUserStatus) {
    if (user == null) return
    logger.warn { "Invoked setUserStatus on user '$user' status $status" }
    userStatus[user] = status
  }

  fun getUser(username: String): User? {
    return userService.loadUserByUsernameOrNull(username)
  }

  fun addOnlineUser(user: User, session: WebSocketSession) {
    logger.warn { "Invoked addOnlineUser for user '$user'" }
    onlineUsers[session] = user
    setOnlineInDatabase(user)
    setUserStatus(user, OnlineUserStatus.READY_FOR_CALL)
  }

  fun addTalk(from: WebSocketSession, to: WebSocketSession) {
    channelForwarding[from] = to
    channelForwarding[to] = from

    setUserStatus(onlineUsers[from], OnlineUserStatus.CALL_IN_PROGRESS)
    setUserStatus(onlineUsers[to], OnlineUserStatus.CALL_IN_PROGRESS)
  }

  fun removeTalk(one: WebSocketSession) {
    val talk = getTalk(one) ?: return

    channelForwarding.remove(talk.key)
    channelForwarding.remove(talk.value)

    setUserStatus(onlineUsers[talk.key], OnlineUserStatus.READY_FOR_CALL)
    setUserStatus(onlineUsers[talk.value], OnlineUserStatus.READY_FOR_CALL)
  }

  fun getTalk(one: WebSocketSession) = channelForwarding
      .filter { it.key == one || it.value == one }
      .entries
      .firstOrNull()

  fun getOnlineUser(session: WebSocketSession): User? {
    return onlineUsers[session]
  }

  fun getOnlineUser(id: Long): User? = onlineUsers.values.firstOrNull { it.id == id }

  fun getOnlineSession(user: User) = onlineUsers.filter { it.value.id == user.id }.keys.firstOrNull()

  fun dropOnlineUser(session: WebSocketSession) {
    val user = onlineUsers[session]!!
    onlineUsers.remove(session)
    logger.warn { "Removed websocket session $session" }
    setOfflineInDatabase(user)
  }

  fun getOnlineUsers(): Map<WebSocketSession, User> {
    return onlineUsers.toMutableMap()
  }

  companion object : KLogging()
}