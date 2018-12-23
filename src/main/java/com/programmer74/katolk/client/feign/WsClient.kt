package com.programmer74.katolk.client.feign

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class WsClient (val username: String, val password: String, val url: String) {

  val headers = HashMap<String, String>()

  val client : WebSocketClient

  val stringConsumers = ArrayList<Consumer<String>>()

  val reconnectThread: Thread

  init {
    val uri = URI(url)
    val creds = "$username:$password"
    val base64creds = Base64.getEncoder().encodeToString(creds.toByteArray())
    headers.put("Authorization", "Basic $base64creds")
    client = object : WebSocketClient(uri, headers) {
      override fun onOpen(serverHandshake: ServerHandshake) {
        println("OPENED")
        this.send("AUTH $username $password")
      }

      override fun onMessage(s: String) {
        println("MESSAGE $s")
        stringConsumers.forEach { it.accept(s) }
      }

      override fun onClose(i: Int, s: String, b: Boolean) {
        println("CLOSED")
      }

      override fun onError(e: Exception) {
        println("ERROR " + e.toString())
        e.printStackTrace()
      }
    }

    reconnectThread = Thread {
      if (!client.isOpen) {
        open()
      } else {
        Thread.sleep(1000)
      }
    }
  }

  fun open() {
    client.connectBlocking(20, TimeUnit.SECONDS)
    reconnectThread.start()
  }

  fun close() {
    client.closeBlocking()
  }

  fun add(consumer: Consumer<String>) {
    stringConsumers.add(consumer)
  }
}