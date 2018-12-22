package com.programmer74.katolk.common.data

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Message(
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  val id: Int = 0,
  val author: Int = 0,
  val dialogueID: Int = 0,
  val body: String = "",
  var date: Long = 0
)