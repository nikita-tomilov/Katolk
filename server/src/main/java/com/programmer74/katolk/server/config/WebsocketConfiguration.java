package com.programmer74.katolk.server.config;

import com.programmer74.katolk.server.controllers.WebsocketHandler;
import com.programmer74.katolk.server.repositories.DialogVault;
import com.programmer74.katolk.server.repositories.UserVault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfiguration implements WebSocketConfigurer {

  public final UserVault userVault;

  public final DialogVault dialogVault;

  @Autowired
  public WebsocketConfiguration(UserVault userVault, DialogVault dialogVault) {
    this.userVault = userVault;
    this.dialogVault = dialogVault;
  }

  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(new WebsocketHandler(userVault, dialogVault), "/api/ws/websocket");
  }
}