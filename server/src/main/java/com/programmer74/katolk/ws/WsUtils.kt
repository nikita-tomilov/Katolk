package com.programmer74.katolk.ws

import com.programmer74.katolk.dto.ClientBinaryMessage
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

fun WebSocketSession.secureSendMessage(msg: WebSocketMessage<*>) {
  synchronized(this) {
    try {
      sendMessage(msg)
    } catch (e: Exception) {
      WebsocketHandler.logger.error(e) { "Error in secureSendMessage" }
    }
  }
}

fun WebSocketSession.secureSendMessage(msg: ClientBinaryMessage) {
  secureSendMessage(BinaryMessage(msg.toBytes()))
}
