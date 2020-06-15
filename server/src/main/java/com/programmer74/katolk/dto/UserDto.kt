package com.programmer74.katolk.dto

import com.programmer74.katolk.dao.User
import com.programmer74.katolk.repository.OnlineUserRepositorySingleton

data class UserDto(
  val id: Long = 0,
  val username: String = "",
  val online: Boolean = false,
  val name: String = "",
  val surname: String = "",
  val born: Long = 0,
  val gender: Char = '?',
  val city: String = "",
  val roles: List<String>,
  val lastOnline: Long = 0
) {
  companion object {
    fun from(user: User): UserDto {
      return UserDto(user.id!!,
          user.username,
          OnlineUserRepositorySingleton.get().isUserOnline(user),
          "name ${user.id}",
          "surname ${user.id}",
          0L,
          '?',
          "city",
          user.authorities.map { it.authority },
          0L)
      //          user.online, user.name, user.surname, user.born, user.gender, user.city, user.lastOnline)
    }
  }
}