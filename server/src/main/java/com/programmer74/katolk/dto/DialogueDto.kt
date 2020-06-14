package com.programmer74.katolk.dto

data class DialogueDto(
  val id: Long = 0,
  val creator: Long = 0,
  val name: String = "",
  val participants: List<UserDto> = emptyList(),
  val latestMessage: MessageDto? = null,
  val unreadCount: Int = 0
)