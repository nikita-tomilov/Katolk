package com.programmer74.katolk.repository

import com.programmer74.katolk.dao.User
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap

object OnlineUserRepositorySingleton {
  private val repo = OnlineUserRepository()

  fun get() = repo
}

class OnlineUserRepository {
  private val onlineUsers = ConcurrentHashMap<Long, User>()

  fun addUser(user: User) {
    onlineUsers[user.safeId()] = user
  }

  fun removeUser(user: User) {
    onlineUsers.remove(user.safeId())
  }

  fun isUserOnline(user: User): Boolean {
    val result = onlineUsers.containsKey(user.safeId())
    return result
  }

  companion object : KLogging()
}
