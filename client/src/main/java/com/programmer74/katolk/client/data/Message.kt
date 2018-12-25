package com.programmer74.katolk.client.data

data class Message(
  val id: Int = 0,
  val author: Int = 0,
  val dialogueID: Int = 0,
  val body: String = "",
  var date: Long = 0,
  var wasRead: Boolean = false
)