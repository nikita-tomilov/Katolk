package com.programmer74.katolk.client.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebsocketTest {
  public static void main(String[] args) {

    URI uri;
    try {
      uri = new URI("http://localhost:8080/api/ws/websocket");
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return;
    }

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Basic YWRtaW46YWRtaW4=");
    WebSocketClient mWebSocketClient = new WebSocketClient(uri, headers) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("OPENED");
        this.send("AUTH admin admin");
      }

      @Override
      public void onMessage(String s) {
        System.out.println("MESSAGE" + s);
      }

      @Override
      public void onClose(int i, String s, boolean b) {
        System.out.println("CLOSED");
      }

      @Override
      public void onError(Exception e) {
        System.out.println("ERROR " + e.toString());
        e.printStackTrace();
      }
    };

    while (true) {
      try {
        Thread.sleep(5000);
      } catch (Exception ex) {

      }
      if (!mWebSocketClient.isOpen()) {
        try {
          mWebSocketClient.connectBlocking(20, TimeUnit.SECONDS);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      mWebSocketClient.send("test");
    }

  }
}
