package com.programmer74.katolk

import com.programmer74.katolk.dto.UserInfoDto
import com.programmer74.katolk.ws.KatolkBinaryMessage
import com.programmer74.katolk.ws.KatolkBinaryMessageType
import mu.KLogging
import java.util.concurrent.Executors
import java.util.function.BiConsumer
import java.util.function.Consumer

class KatolkCallModel(
  private val katolkModel: KatolkModel
) {

  private val executor = Executors.newFixedThreadPool(4)

  var onUserDialsYouCallback = Consumer<UserInfoDto> {}

  var onCallAcceptedCallback = Consumer<UserInfoDto> {}

  var onCallDeniedCallback = Consumer<UserInfoDto> {}

  var onCallBeginCallback = Consumer<UserInfoDto> {}

  var onCallEndCallback = Consumer<String> { }

  var onCallErrorCallback = Consumer<String> { }

  init {
    katolkModel.getWsClient().addBinary(Consumer { wsBinaryMsgHandler(it) })
  }

  //TODO: maybe send full user info instead of id?
  private fun wsBinaryMsgHandler(msg: KatolkBinaryMessage) {
    if (ignoreMessages.contains(msg.type)) return
    logger.warn { "MSG OF TYPE ${msg.type}" }

    when (msg.type) {
      KatolkBinaryMessageType.CALL_REQUEST -> {
        val askerUserID = msg.intPayload().toLong()
        katolkModel.getUserInfo(askerUserID, Consumer { asker ->
          onUserDialsYouCallback.accept(asker)
        })
      }
      KatolkBinaryMessageType.CALL_BEGIN -> {
        val askerUserID = msg.intPayload().toLong()
        katolkModel.getUserInfo(askerUserID, Consumer { asker ->
          onCallBeginCallback.accept(asker)
        })
      }
      KatolkBinaryMessageType.CALL_RESPONSE_ALLOW -> {
        val askerUserID = msg.intPayload().toLong()
        katolkModel.getUserInfo(askerUserID, Consumer { asker ->
          onCallAcceptedCallback.accept(asker)
        })
      }
      KatolkBinaryMessageType.CALL_RESPONSE_DENY -> {
        val askerUserID = msg.intPayload().toLong()
        katolkModel.getUserInfo(askerUserID, Consumer { asker ->
          onCallDeniedCallback.accept(asker)
        })
      }
      KatolkBinaryMessageType.CALL_END -> {
        onCallEndCallback.accept("Call ended")
      }
      KatolkBinaryMessageType.CALL_ERROR -> {
        onCallErrorCallback.accept(String(msg.payload))
      }
      KatolkBinaryMessageType.CALL_END_ABNORMAL -> {
        onCallEndCallback.accept("Abnormal call ending. Probably opponent disconnected")
      }
      else -> {}
    }
  }

  fun dialUser(userID: Long) {
    executor.submit {
      val callMessage = KatolkBinaryMessage(
          KatolkBinaryMessageType.CALL_REQUEST,
          userID.toInt())
      katolkModel.getWsClient().client.send(callMessage.toBytes())
    }
  }

  fun acceptCall(userID: Long) {
    executor.submit {
      katolkModel.getWsClient().send(
          KatolkBinaryMessage(
              KatolkBinaryMessageType.CALL_RESPONSE_ALLOW,
              userID.toInt()))
    }
  }

  fun denyCall(userID: Long) {
    executor.submit {
      katolkModel.getWsClient().send(
          KatolkBinaryMessage(
              KatolkBinaryMessageType.CALL_RESPONSE_DENY,
              userID.toInt()))
    }
  }

  fun endCall(userID: Long) {
    executor.submit {
      katolkModel.getWsClient().send(
          KatolkBinaryMessage(
              KatolkBinaryMessageType.CALL_END,
              userID.toInt()))
    }
  }

  private val ignoreMessages = setOf(
      KatolkBinaryMessageType.PING_SERVER_REQUEST,
      KatolkBinaryMessageType.PING_SERVER_RESPONSE,
      KatolkBinaryMessageType.PING_COMPANION_REQUEST,
      KatolkBinaryMessageType.PING_COMPANION_RESPONSE,
      KatolkBinaryMessageType.PACKET_AUDIO)

  companion object : KLogging()
}