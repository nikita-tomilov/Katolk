package com.programmer74.katolk.client.feign

import com.programmer74.katolk.common.data.DialogueJson
import com.programmer74.katolk.common.data.Message
import com.programmer74.katolk.common.data.MessageJson
import feign.Param
import feign.RequestLine

interface DialogueClient {
  @RequestLine("GET /list")
  fun getDialogs(): List<DialogueJson>

  @RequestLine("GET /messages/{id}")
  fun getMessages(@Param("id") id: Int): List<MessageJson>

  @RequestLine("POST /messages/send")
  fun sendMessage(msg: Message): Message
}