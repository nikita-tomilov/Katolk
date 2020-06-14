package com.programmer74.katolk.service

import com.programmer74.katolk.dao.User
import mu.KLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

@Service
class OnlineUserService(
  private val userService: UserService,
  private val userPasswordEncoder: PasswordEncoder
) {
  private val onlineUsers = HashMap<WebSocketSession, User>()

  private val talkingUserSessions = HashMap<WebSocketSession, WebSocketSession>()

  fun setOnlineInDatabase(user: User) {
    logger.warn { "Invoked setOnline on user $user" }
  }

  fun setOfflineInDatabase(user: User) {
    logger.warn { "Invoked setOffline on user $user" }
  }

  fun checkPasswordMatches(user: User, password: String): Boolean {
    return userPasswordEncoder.matches(password, user.password)
  }

  fun getUser(id: Long): User {
    return userService.findUserById(id)
  }

  fun getUser(username: String): User? {
    return userService.loadUserByUsernameOrNull(username)
  }

  fun addOnlineUser(user: User, session: WebSocketSession) {
    logger.warn { "Invoked addOnlineUser for user $user" }
    onlineUsers[session] = user
    setOnlineInDatabase(user)
  }

  fun addTalk(from: WebSocketSession, to: WebSocketSession) {
    talkingUserSessions[from] = to
  }

  fun removeTalk(one: WebSocketSession) {
    if (talkingUserSessions.containsKey(one)) {
      talkingUserSessions.remove(one)
    } else {
      val two
          = talkingUserSessions.filter { it.value == one }.keys.first()
      talkingUserSessions.remove(two)
    }
  }

  fun getTalk(one: WebSocketSession) = talkingUserSessions
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