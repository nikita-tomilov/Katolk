package com.programmer74.katolk.dto

data class UserInfoDto(
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
)