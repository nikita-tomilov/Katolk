package com.programmer74.katolk.controllers

import com.programmer74.katolk.dao.DialogueEntity
import com.programmer74.katolk.dao.MessageEntity
import com.programmer74.katolk.dto.DialogueDto
import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.service.DialogService
import com.programmer74.katolk.service.MessagesService
import com.programmer74.katolk.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/dialog"])
class DialogController(
  private val userService: UserService,
  private val messagesService: MessagesService,
  private val dialogService: DialogService
) {

  //  @Autowired
  //  lateinit var websocketHandler: WebsocketHandler

  @ResponseStatus(HttpStatus.FORBIDDEN)
  inner class ForbiddenException : RuntimeException()

  @GetMapping("/all")
  fun dialogues(): List<DialogueEntity> {
    val me = userService.meAsEntity()
    return dialogService.getDialogs(me)
  }

  @GetMapping("/list")
  fun dialoguesAsList(): List<DialogueDto> {
    val me = userService.meAsEntity()
    return dialogService.getDialogRepresentations(me)
  }

  @GetMapping("/messages/{dialogueId}")
  fun messagesInDialogue(
    @PathVariable("dialogueId") dialogueId: Long
  ): List<MessageDto> {
    val me = userService.meAsEntity()
    return dialogService.getMessagesInDialogueAsDto(me.safeId(), dialogueId)
  }

  @GetMapping("/messages/{dialogueId}/markread")
  fun markMessagesInDialogueAsRead(
    @PathVariable("dialogueId") dialogueId: Long
  ): List<MessageDto> {
    val me = userService.meAsEntity()
    dialogService.markMessagesInDialogueAsRead(me.safeId(), dialogueId)
    return emptyList()
  }

  @PostMapping("/messages/send")
  fun sendMessage(
    @RequestBody msg: MessageEntity
  ): MessageEntity {
    val me = userService.meAsEntity()
    return dialogService.sendMessage(me, msg)
  }
}