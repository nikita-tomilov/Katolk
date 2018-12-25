package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.server.entity.UserEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import javax.annotation.PostConstruct

@Service
class UserVault(val repository: UserRepository,
                val passwordEncoder: PasswordEncoder) {

  private val onlineUsers = HashMap<WebSocketSession, UserEntity>()

  fun saveUser(user: UserEntity) {
    user.password = passwordEncoder.encode(user.password)
    repository.save(user)
  }

  fun setOnline(user: UserEntity) {
    user.online = true
    repository.save(user)
  }

  fun setOffline(user: UserEntity) {
    user.online = false
    repository.save(user)
  }

  fun checkPasswordMatches(user: UserEntity, password: String): Boolean {
    return passwordEncoder.matches(password, user.password)
  }

  fun getCurrentUser(): UserEntity {
    val context = SecurityContextHolder.getContext()
    val username = context.authentication.principal as String
    val user = repository.findByUsername(username)!!
    val userCopy = user.copy()
    userCopy.password = ""
    return userCopy
  }

  fun getUser(id: Int): UserEntity {
    val user = repository.findById(id).get()
    val userCopy = user.copy()
    userCopy.password = ""
    return userCopy
  }

  fun addOnlineUser(user: UserEntity, session: WebSocketSession) {
    System.err.println("UserEntity ${user.username} online")
    onlineUsers[session] = user
    setOnline(user)
  }

  fun getOnlineUser(session: WebSocketSession): UserEntity? {
    return onlineUsers[session]
  }

  fun dropOnlineUser(session: WebSocketSession) {
    val user = onlineUsers[session]!!
    onlineUsers.remove(session)
    setOffline(user)
    System.err.println("Removed ${session}")
  }

  fun getOnlineUsers(): Map<WebSocketSession, UserEntity> {
    return onlineUsers.toMutableMap()
  }

  @PostConstruct
  fun initUsers() {
    saveUser(UserEntity(0, "admin", "admin",
        false, "Admin", "Adminoff", 977721768000L, 'M', "City", 0))
    saveUser(UserEntity(0, "user1", "user1",
        false, "First", "User", 883027368000L, 'M', "City", 0))
    saveUser(UserEntity(0, "user2", "user2",
        false, "Second", "User", 819868968000L, 'F', "City", 0))
  }
}