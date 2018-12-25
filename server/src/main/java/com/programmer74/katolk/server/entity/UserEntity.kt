package com.programmer74.katolk.server.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class UserEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  val id: Int = 0,
  val username: String = "",
  var password: String = "",
  var online: Boolean = false,
  val name: String = "",
  val surname: String = "",
  val born: Long = 0,
  val gender: Char = '?',
  val city: String = "",
  var lastOnline: Long = 0
)