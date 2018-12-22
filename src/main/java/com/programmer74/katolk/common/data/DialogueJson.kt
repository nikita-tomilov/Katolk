package com.programmer74.katolk.common.data

data class DialogueJson(
  val id: Int = 0,
  val creator: Int = 0,
  val participants: List<String> = emptyList(),
  val latestMessage: MessageJson? = null
) {
  override fun toString(): String {
    return participants.joinToString(", ") + " " + latestMessage?.body
  }
}