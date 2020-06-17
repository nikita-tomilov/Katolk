package com.programmer74.katolk.ws

import com.programmer74.katolk.dao.User

data class WebsocketNotification(
  val user: User,
  val payload: WebsocketNotificationPayload
)