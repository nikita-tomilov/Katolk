package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.common.data.Dialogue
import com.programmer74.katolk.common.data.Message
import com.programmer74.katolk.common.data.MessageJson
import com.programmer74.katolk.common.data.User
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MessageVault(val messages: MessageRepository,
                   val users: UserVault) {
  fun latestMessageInDialogue(dialogue: Dialogue): Message? {
    return messages.findFirstByDialogueIDOrderByDateDesc(dialogue.id)
  }

  fun latestMessageInDialogueAsJson(dialogue: Dialogue): MessageJson? {
    return latestMessageInDialogue(dialogue)?.let { getMessageRepresentation(it) }
  }

  fun messagesInDialogue(dialogue: Dialogue): List<Message> {
    return messages.findByDialogueIDOrderByDateDesc(dialogue.id)
  }

  fun messagesInDialogueAsJson(dialogue: Dialogue): List<MessageJson> {
    return messagesInDialogue(dialogue).map { it -> getMessageRepresentation(it) }
  }

  fun sendMessage(author: User, dialogue: Dialogue, body: String): Message {
    val message = Message(0, author.id, dialogue.id, body, Instant.now().toEpochMilli())
    messages.save(message)
    return message
  }

  fun getMessageRepresentation(msg: Message): MessageJson {
    val author = users.repository.findById(msg.author).get()
    return MessageJson(msg.id, author.username, author.id,
        msg.dialogueID, msg.body, msg.date)
  }
}