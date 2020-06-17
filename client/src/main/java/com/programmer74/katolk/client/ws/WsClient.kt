package com.programmer74.katolk.client.ws

import com.programmer74.katolk.client.binary.BinaryMessage
import com.programmer74.katolk.client.binary.BinaryMessageType
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class WsClient(
  private val username: String,
  private val password: String,
  private val url: String,
  private val token: String
) {

  val headers = HashMap<String, String>()

  val client: WebSocketClient

  val stringConsumers = ArrayList<Consumer<String>>()

  val binaryConsumers = ArrayList<Consumer<BinaryMessage>>()

  lateinit var reconnectThread: Thread

  lateinit var pingThread: Thread

  var isOpponentAvailable = false

  init {
    val uri = URI(url)
    headers.put("Authorization", "Bearer $token")
    client = object : WebSocketClient(uri, headers) {
      override fun onOpen(serverHandshake: ServerHandshake) {
        println("OPENED")
        this.send("AUTH $username $password")
      }

      override fun onMessage(s: String) {
        println("MESSAGE \"$s\"")
        stringConsumers.forEach { it.accept(s) }
      }

      override fun onMessage(bytes: ByteBuffer?) {
        val payload = bytes!!.array()
        val message = BinaryMessage.fromBytes(payload)
        //        println("BINARY MESSAGE \"${message.type.toString()}\"")
        if (message.type == BinaryMessageType.PING_COMPANION_REQUEST) {
          message.type = BinaryMessageType.PING_COMPANION_RESPONSE
          send(message.toBytes())
        } else {
          binaryConsumers.forEach { it.accept(message) }
        }
      }

      override fun onClose(i: Int, s: String, b: Boolean) {
        println("CLOSED")
      }

      override fun onError(e: Exception) {
        println("ERROR " + e.toString())
        e.printStackTrace()
      }
    }

    setupReconnectThread()
    setupPingThread()
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

  fun addBinary(consumer: Consumer<BinaryMessage>) {
    binaryConsumers.add(consumer)
  }

  fun send(message: BinaryMessage) {
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

      val latestMessageRef = AtomicReference<BinaryMessage?>()
      binaryConsumers.add(Consumer<BinaryMessage> {
        if ((it.type == BinaryMessageType.PING_SERVER_RESPONSE) ||
            (it.type == BinaryMessageType.PING_COMPANION_RESPONSE)
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

  private fun serverPing(latestMessageRef: AtomicReference<BinaryMessage?>) {
    val begin = System.currentTimeMillis()
    latestMessageRef.set(null)
    send(BinaryMessage(BinaryMessageType.PING_SERVER_REQUEST, ByteArray(0)))
    var end = System.currentTimeMillis()
    while ((latestMessageRef.get() == null) ||
        (latestMessageRef.get()!!.type == BinaryMessageType.PING_COMPANION_RESPONSE)
    ) {
      Thread.yield()
      end = System.currentTimeMillis()
      if ((end - begin) > 20000) {
        println("TIMEOUT MS")
        break
      }
    }
    println("PING TO SRV IS ${end - begin} ms")
  }

  private fun opponentPing(latestMessageRef: AtomicReference<BinaryMessage?>) {
    val begin = System.currentTimeMillis()
    latestMessageRef.set(null)
    send(BinaryMessage(BinaryMessageType.PING_COMPANION_REQUEST, ByteArray(0)))
    var end = System.currentTimeMillis()
    while ((latestMessageRef.get() == null) ||
        (latestMessageRef.get()!!.type == BinaryMessageType.PING_SERVER_RESPONSE)
    ) {
      Thread.yield()
      end = System.currentTimeMillis()
      if ((end - begin) > 20000) {
        println("TIMEOUT MS")
        break
      }
    }
    println("PING TO OPPONENT IS ${end - begin} ms")
  }
}