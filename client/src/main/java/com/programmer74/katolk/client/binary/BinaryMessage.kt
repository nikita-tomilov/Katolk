package com.programmer74.katolk.client.binary

enum class BinaryMessageType {
  CALL_REQUEST,
  CALL_RESPONSE_ALLOW,
  CALL_RESPONSE_DENY,
  PING_SERVER_REQUEST,
  PING_SERVER_RESPONSE,
  PING_COMPANION_REQUEST,
  PING_COMPANION_RESPONSE
}

data class BinaryMessage(
  val type: BinaryMessageType,
  val payload: ByteArray
) {

  fun toBytes(): ByteArray {
    val header = ByteArray(additionalLen)
    header[0] = type.ordinal.toByte()
    return header.plus(payload)
  }

  companion object {

    val additionalLen = 1

    fun fromBytes(bytes: ByteArray): BinaryMessage {
      val payload = bytes.copyOfRange(additionalLen, bytes.size - additionalLen + 1)
      val type = BinaryMessageType.values()[bytes[0].toInt()]
      return BinaryMessage(type, payload)
    }
  }
}