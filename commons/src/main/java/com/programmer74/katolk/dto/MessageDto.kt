package com.programmer74.katolk.dto

data class MessageDto(
  val id: Long = 0,
  val author: String = "",
  val authorId: Long = 0,
  val dialogueID: Long = 0,
  val body: String = "",
  val date: Long = 0,
  val wasRead: Boolean = false
)