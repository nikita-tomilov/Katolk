package com.programmer74.katolk.server.entity

data class UserJson(
  val id: Int = 0,
  val username: String = "",
  val online: Boolean = false,
  val name: String = "",
  val surname: String = "",
  val born: Long = 0,
  val gender: Char = '?',
  val city: String = "",
  val lastOnline: Long = 0
) {
  companion object {
    fun from(user: UserEntity): UserJson {
      return UserJson(user.id, user.username,
          user.online, user.name, user.surname, user.born, user.gender, user.city, user.lastOnline)
    }
  }
}