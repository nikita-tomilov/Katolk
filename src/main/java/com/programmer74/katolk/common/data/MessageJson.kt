package com.programmer74.katolk.common.data

data class MessageJson(
  val id: Int = 0,
  val author: String = "",
  val authorId: Int = 0,
  val dialogueID: Int = 0,
  val body: String = "",
  var date: Long = 0
)