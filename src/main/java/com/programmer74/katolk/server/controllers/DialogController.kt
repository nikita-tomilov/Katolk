package com.programmer74.katolk.server.controllers

import com.programmer74.katolk.common.data.Dialogue
import com.programmer74.katolk.common.data.DialogueJson
import com.programmer74.katolk.common.data.Message
import com.programmer74.katolk.common.data.MessageJson
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
  fun dialogues(): List<Dialogue> {
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

  @PostMapping("/messages/send")
  fun sendMessage(
      @RequestBody msg: Message
  ): Message {
    val me = users.getCurrentUser()
    val dialogueId = msg.dialogueID
    val participants = dialogs.participants.findAllByDialogueID(dialogueId)!!
    if (participants.none { it.userID == me.id }) {
      throw ForbiddenException()
    }
    val dialogue = dialogs.dialogs.findById(dialogueId).get()
    val message =  messages.sendMessage(me, dialogue, msg.body)

    val userIds = participants.map { it.userID }
    val users = users.repository.findAllById(userIds).toList()

    users.forEach { websocketHandler.notifyUserAboutNewMessage(it) }

    return message
  }
}