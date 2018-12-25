package com.programmer74.katolk.server.entity

import java.util.*

enum class ClientBinaryMessageType {
  CALL_REQUEST,
  CALL_RESPONSE_ALLOW,
  CALL_RESPONSE_DENY,
  PING_SERVER_REQUEST,
  PING_SERVER_RESPONSE,
  PING_COMPANION_REQUEST,
  PING_COMPANION_RESPONSE
}

data class ClientBinaryMessage(
    val type: ClientBinaryMessageType,
    val payload: ByteArray,
    val fromUser: UserEntity
) {

  fun toBytes(): ByteArray {
    val header = ByteArray(additionalLen)
    header[0] = type.ordinal.toByte()
    return header.plus(payload)
  }

  companion object {

    val additionalLen = 1

    fun fromBytes(bytes: ByteArray, from: UserEntity): ClientBinaryMessage {
      val payload = bytes.copyOfRange(additionalLen, bytes.size - additionalLen + 1)
      val type = ClientBinaryMessageType.values()[bytes[0].toInt()]
      return ClientBinaryMessage(type, payload, from)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ClientBinaryMessage

    if (type != other.type) return false
    if (!Arrays.equals(payload, other.payload)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + Arrays.hashCode(payload)
    return result
  }

}