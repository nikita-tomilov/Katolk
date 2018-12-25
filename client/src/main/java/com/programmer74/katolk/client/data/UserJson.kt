package com.programmer74.katolk.client.data

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
)