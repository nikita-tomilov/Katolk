package com.programmer74.katolk.server.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class MessageEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  val id: Int = 0,
  val author: Int = 0,
  val dialogueID: Int = 0,
  val body: String = "",
  var date: Long = 0,
  var wasRead: Boolean = false
)