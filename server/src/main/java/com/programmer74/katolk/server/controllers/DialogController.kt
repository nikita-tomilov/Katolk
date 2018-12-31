package com.programmer74.katolk.server.controllers

import com.programmer74.katolk.server.entity.DialogueEntity
import com.programmer74.katolk.server.entity.DialogueJson
import com.programmer74.katolk.server.entity.MessageEntity
import com.programmer74.katolk.server.entity.MessageJson
import com.programmer74.katolk.server.repositories.DialogVault
import com.programmer74.katolk.server.repositories.MessageVault
import com.programmer74.katolk.server.repositories.UserVault
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus

@RestController
@RequestMapping(path = ["/api/dialog"])
class DialogController {

  @Autowired
  lateinit var users: UserVault

  @Autowired
  lateinit var messages: MessageVault

  @Autowired
  lateinit var dialogs: DialogVault

  @Autowired
  lateinit var websocketHandler: WebsocketHandler

  @ResponseStatus(HttpStatus.FORBIDDEN)
  inner class ForbiddenException : RuntimeException()

  @GetMapping("/all")
  fun dialogues(): List<DialogueEntity> {
    val me = users.getCurrentUser()
    return dialogs.getDialogs(me)
  }

  @GetMapping("/list")
  fun dialoguesAsList(): List<DialogueJson> {
    val me = users.getCurrentUser()
    return dialogs.getDialogRepresentations(me)
  }

  @GetMapping("/messages/{id}")
  fun messagesInDialogue(
      @PathVariable("id") id: Int
  ): List<MessageJson> {
    val me = users.getCurrentUser()
    val participants = dialogs.participants.findAllByDialogueID(id)!!
    if (participants.none { it.userID == me.id }) {
      return emptyList()
    }
    val dialogue = dialogs.dialogs.findById(id).get()
    return messages.messagesInDialogueAsJson(dialogue)
  }

  @GetMapping("/messages/{id}/markread")
  fun markMessagesInDialogueAsRead(
      @PathVariable("id") id: Int
  ): List<MessageJson> {
    val me = users.getCurrentUser()
    val participants = dialogs.participants.findAllByDialogueID(id)!!
    if (participants.none { it.userID == me.id }) {
      return emptyList()
    }
    val dialogue = dialogs.dialogs.findById(id).get()
    val unreadMessages
        = messages.messages.findByDialogueIDAndWasRead(dialogue.id, false)
        .filter { it.author != me.id }
    if (unreadMessages.isNotEmpty()) {
      messages.markMessagesInDialogueAsRead(unreadMessages)

      val userIds = participants.map { it.userID }
      val users = users.repository.findAllById(userIds)
          .toList()

      users.forEach { websocketHandler.notifyUserAboutNewMessage(it) }
    }

    return emptyList() //smth else maybe?
  }

  @PostMapping("/messages/send")
  fun sendMessage(
      @RequestBody msg: MessageEntity
  ): MessageEntity {
    val me = users.getCurrentUser()
    val dialogueId = msg.dialogueID
    val participants = dialogs.participants.findAllByDialogueID(dialogueId)!!
    if (participants.none { it.userID == me.id }) {
      throw ForbiddenException()
    }

    val dialogue = dialogs.dialogs.findById(dialogueId).get()
    val message =  messages.sendMessage(me, dialogue, msg.body)

    if (participants.size == 1) {
      message.wasRead = true
      messages.messages.save(message)
    }

    val userIds = participants.map { it.userID }
    val users = users.repository.findAllById(userIds).toList()

    users.forEach { websocketHandler.notifyUserAboutNewMessage(it) }

    return message
  }
}