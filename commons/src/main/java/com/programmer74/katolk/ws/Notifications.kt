package com.programmer74.katolk.ws

enum class NotificationType {
  NEW_MESSAGE, USER_STATE_CHANGED
}

data class WebsocketNotificationPayload(
  val type: NotificationType,
  val payload: Map<String, Any>
)