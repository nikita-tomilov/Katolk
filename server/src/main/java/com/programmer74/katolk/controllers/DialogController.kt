package com.programmer74.katolk.controllers

import com.programmer74.katolk.api.DialogueAPI
import com.programmer74.katolk.dto.DialogueDto
import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.dto.MessageRequestDto
import com.programmer74.katolk.service.DialogueService
import com.programmer74.katolk.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/dialog"])
class DialogController(
  private val userService: UserService,
  private val dialogService: DialogueService
) : DialogueAPI {

  @GetMapping("/list")
  override fun getDialogs(): List<DialogueDto> {
    val me = userService.meAsEntity()
    return dialogService.getDialogRepresentations(me)
  }

  @GetMapping("/messages/{dialogueId}")
  override fun messagesInDialogue(
    @PathVariable("dialogueId") dialogueId: Long
  ): List<MessageDto> {
    val me = userService.meAsEntity()
    return dialogService.getMessagesInDialogueAsDto(me.safeId(), dialogueId)
  }

  @GetMapping("/messages/{dialogueId}/markread")
  override fun markMessagesInDialogueAsRead(
    @PathVariable("dialogueId") dialogueId: Long
  ): List<MessageDto> {
    val me = userService.meAsEntity()
    dialogService.markMessagesInDialogueAsRead(me.safeId(), dialogueId)
    return emptyList()
  }

  @PostMapping("/messages/send")
  override fun sendMessage(
    @RequestBody msg: MessageRequestDto
  ): MessageDto {
    val me = userService.meAsEntity()
    return dialogService.sendMessageAndReturnDto(me, msg)
  }

  @GetMapping("/create/{userId}")
  override fun getDialogs(
    @PathVariable("userId") userId: Long
  ): List<DialogueDto> {
    val me = userService.meAsEntity()
    val another = userService.findUserById(userId)
    dialogService.createDialogue(me, another)
    return dialogService.getDialogRepresentations(me)
  }
}