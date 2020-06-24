package com.programmer74.katolk.api

import com.programmer74.katolk.dto.DialogueDto
import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.dto.MessageRequestDto
import feign.Param
import feign.RequestLine

interface DialogueAPI {
  @RequestLine("GET /list")
  fun getDialogs(): List<DialogueDto>

  @RequestLine("GET /messages/{dialogueId}")
  fun messagesInDialogue(
    @Param("dialogueId") dialogueId: Long
  ): List<MessageDto>

  @RequestLine("GET /messages/{dialogueId}/markread")
  fun markMessagesInDialogueAsRead(
    @Param("dialogueId") dialogueId: Long
  ): List<MessageDto>

  @RequestLine("POST /messages/send")
  fun sendMessage(
    msg: MessageRequestDto
  ): MessageDto

  @RequestLine("GET /create/{userId}")
  fun getDialogs(@Param( "userId") userId: Long): List<DialogueDto>
}