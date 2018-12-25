package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.server.entity.DialogueEntity
import com.programmer74.katolk.server.entity.MessageEntity
import com.programmer74.katolk.server.entity.MessageJson
import com.programmer74.katolk.server.entity.UserEntity
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MessageVault(val messages: MessageRepository,
                   val users: UserVault) {
  fun latestMessageInDialogue(dialogue: DialogueEntity): MessageEntity? {
    return messages.findFirstByDialogueIDOrderByDateDesc(dialogue.id)
  }

  fun latestMessageInDialogueAsJson(dialogue: DialogueEntity): MessageJson? {
    return latestMessageInDialogue(dialogue)?.let { getMessageRepresentation(it) }
  }

  fun messagesInDialogue(dialogue: DialogueEntity): List<MessageEntity> {
    return messages.findByDialogueIDOrderByDateDesc(dialogue.id)
  }

  fun messagesInDialogueAsJson(dialogue: DialogueEntity): List<MessageJson> {
    return messagesInDialogue(dialogue).map { it -> getMessageRepresentation(it) }
  }

  fun sendMessage(author: UserEntity, dialogue: DialogueEntity, body: String): MessageEntity {
    val message = MessageEntity(0, author.id, dialogue.id, body,
        Instant.now().toEpochMilli())
    messages.save(message)
    return message
  }

  fun getMessageRepresentation(msg: MessageEntity): MessageJson {
    val author = users.repository.findById(msg.author).get()
    return MessageJson(msg.id, author.username, author.id,
        msg.dialogueID, msg.body, msg.date, msg.wasRead)
  }

  fun markMessagesInDialogueAsRead(unreadMessages: List<MessageEntity>) {
    unreadMessages
        .forEach {
      it.wasRead = true
      messages.save(it)
    }
  }
}