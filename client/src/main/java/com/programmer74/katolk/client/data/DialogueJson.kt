package com.programmer74.katolk.client.data

data class DialogueJson(
  val id: Int = 0,
  val creator: Int = 0,
  val name: String = "",
  val participants: List<UserJson> = emptyList(),
  val latestMessage: MessageJson? = null,
  val unreadCount: Int = 0
)