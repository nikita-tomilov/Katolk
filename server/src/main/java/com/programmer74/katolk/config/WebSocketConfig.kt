package com.programmer74.katolk.config

import com.programmer74.katolk.service.DialogueService
import com.programmer74.katolk.service.OnlineUserService
import com.programmer74.katolk.ws.WebsocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
  private val onlineUserService: OnlineUserService,
  private val dialogueService: DialogueService
) : WebSocketConfigurer {

  override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
    registry.addHandler(handler(), "/api/ws/websocket")
  }

  @Bean
  fun handler(): WebSocketHandler {
    return WebsocketHandler(onlineUserService, dialogueService)
  }
}