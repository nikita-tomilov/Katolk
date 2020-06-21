package com.programmer74.katolk.ws

import mu.KLogging
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class WsClient(
  private val url: String,
  private val token: String
) {

  val headers = HashMap<String, String>()

  lateinit var client: WebSocketClient

  val stringConsumers = ArrayList<Consumer<String>>()

  val binaryConsumers = ArrayList<Consumer<KatolkBinaryMessage>>()

  lateinit var reconnectThread: Thread

  lateinit var pingThread: Thread

  private val executor = Executors.newSingleThreadExecutor()

  var isOpponentAvailable = false

  init {
    headers.put("Authorization", "Bearer $token")

    reconnect()

    setupReconnectThread()
    setupPingThread()
  }

  private fun reconnect() {
    val uri = URI(url)
    client = object : WebSocketClient(uri, headers) {
      override fun onOpen(serverHandshake: ServerHandshake) {
        logger.info { "WebSocket opened" }
        this.send("AUTH")
      }

      override fun onMessage(s: String) {
        logger.info { "WebSocket text message \"$s\"" }
        stringConsumers.forEach { it.accept(s) }
      }

      override fun onMessage(bytes: ByteBuffer?) {
        val payload = bytes!!.array()
        val message = KatolkBinaryMessage.fromBytes(payload)
        if (message.type == KatolkBinaryMessageType.PING_COMPANION_REQUEST) {
          message.type = KatolkBinaryMessageType.PING_COMPANION_RESPONSE
          send(message.toBytes())
        } else {
          binaryConsumers.forEach { it.accept(message) }
        }
      }

      override fun onClose(i: Int, s: String, b: Boolean) {
        logger.info { "WebSocket connection closed" }
        Thread.sleep(3000L)
        executor.submit { reconnect() }
      }

      override fun onError(e: Exception) {
        logger.error(e) { "WebSocket connection error" }
        Thread.sleep(3000L)
      }
    }
  }

  fun open() {
    client.connectBlocking(20, TimeUnit.SECONDS)
    reconnectThread.start()
    pingThread.start()
  }

  fun close() {
    client.closeBlocking()
  }

  fun add(consumer: Consumer<String>) {
    stringConsumers.add(consumer)
  }

  fun addBinary(consumer: Consumer<KatolkBinaryMessage>) {
    binaryConsumers.add(consumer)
  }

  fun send(message: KatolkBinaryMessage) {
    client.send(message.toBytes())
  }

  private fun setupReconnectThread() {
    reconnectThread = Thread {
      while (true) {
        if (!client.isOpen) {
          open()
        } else {
          Thread.sleep(1000)
        }
      }
    }
  }

  private fun setupPingThread() {
    pingThread = Thread {

      val latestMessageRef = AtomicReference<KatolkBinaryMessage?>()
      binaryConsumers.add(Consumer<KatolkBinaryMessage> {
        if ((it.type == KatolkBinaryMessageType.PING_SERVER_RESPONSE) ||
            (it.type == KatolkBinaryMessageType.PING_COMPANION_RESPONSE)
        ) {
          latestMessageRef.set(it)
        }
      })

      while (true) {
        if (!client.isOpen) {
          continue
        } else {
          Thread.sleep(2000)
          serverPing(latestMessageRef)
          Thread.sleep(2000)
          if (isOpponentAvailable) {
            opponentPing(latestMessageRef)
          }
        }
      }
    }
  }

  private fun serverPing(latestMessageRef: AtomicReference<KatolkBinaryMessage?>) {
    val begin = System.currentTimeMillis()
    latestMessageRef.set(null)
    send(
        KatolkBinaryMessage(
            KatolkBinaryMessageType.PING_SERVER_REQUEST,
            ByteArray(0)))
    var end = System.currentTimeMillis()
    while ((latestMessageRef.get() == null) ||
        (latestMessageRef.get()!!.type == KatolkBinaryMessageType.PING_COMPANION_RESPONSE)
    ) {
      Thread.yield()
      end = System.currentTimeMillis()
      if ((end - begin) > 20000) {
        logger.info { "TIMEOUT MS" }
        break
      }
    }
    logger.info { "PING TO SRV IS ${end - begin} ms" }
  }

  private fun opponentPing(latestMessageRef: AtomicReference<KatolkBinaryMessage?>) {
    val begin = System.currentTimeMillis()
    latestMessageRef.set(null)
    send(
        KatolkBinaryMessage(
            KatolkBinaryMessageType.PING_COMPANION_REQUEST,
            ByteArray(0)))
    var end = System.currentTimeMillis()
    while ((latestMessageRef.get() == null) ||
        (latestMessageRef.get()!!.type == KatolkBinaryMessageType.PING_SERVER_RESPONSE)
    ) {
      Thread.yield()
      end = System.currentTimeMillis()
      if ((end - begin) > 20000) {
        logger.info { "TIMEOUT MS" }
        break
      }
    }
    logger.info { "PING TO OPPONENT IS ${end - begin} ms" }
  }

  companion object : KLogging()
}