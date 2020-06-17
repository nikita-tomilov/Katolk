package com.programmer74.katolk.ws

enum class KatolkBinaryMessageType {
  CALL_REQUEST,
  CALL_ERROR,
  CALL_RESPONSE_ALLOW,
  CALL_RESPONSE_DENY,
  CALL_BEGIN,
  CALL_END,
  CALL_END_ABNORMAL,
  PING_SERVER_REQUEST,
  PING_SERVER_RESPONSE,
  PING_COMPANION_REQUEST,
  PING_COMPANION_RESPONSE,
  PACKET_AUDIO
}

data class KatolkBinaryMessage(
  var type: KatolkBinaryMessageType,
  val payload: ByteArray
) {

  constructor(type: KatolkBinaryMessageType, payload: Int):
      this(
          type,
          byteArrayOf(
              payload.ushr(24).toByte(),
              payload.ushr(16).toByte(),
              payload.ushr(8) .toByte(),
              payload.toByte()
          ))

  fun toBytes(): ByteArray {
    val header = ByteArray(additionalLen)
    header[0] = type.ordinal.toByte()
    return header.plus(payload)
  }

  fun intPayload(): Int {
    return payload[0].toInt() shl 24 or
        (payload[1].toInt() and 0xFF shl 16) or
        (payload[2].toInt() and 0xFF shl 8) or
        (payload[3].toInt() and 0xFF)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as KatolkBinaryMessage

    if (type != other.type) return false
    if (!payload.contentEquals(other.payload)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + payload.contentHashCode()
    return result
  }

  companion object {

    val additionalLen = 1

    fun fromBytes(bytes: ByteArray): KatolkBinaryMessage {
      val payload = bytes.copyOfRange(additionalLen, bytes.size - additionalLen + 1)
      val type = KatolkBinaryMessageType.values()[bytes[0].toInt()]
      return KatolkBinaryMessage(type, payload)
    }
  }
}