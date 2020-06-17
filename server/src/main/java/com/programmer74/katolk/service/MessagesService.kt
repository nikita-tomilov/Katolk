package com.programmer74.katolk.service

import com.programmer74.katolk.dao.DialogueEntity
import com.programmer74.katolk.dao.MessageEntity
import com.programmer74.katolk.dao.User
import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.repository.MessageRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MessagesService(
  private val messageRepository: MessageRepository,
  private val usersService: UserService
) {
  fun latestMessageInDialogue(dialogue: DialogueEntity): MessageEntity? {
    return messageRepository.findFirstByDialogueIDOrderByTimestampDesc(dialogue.safeId())
  }

  fun latestMessageInDialogueAsJson(dialogue: DialogueEntity): MessageDto? {
    return latestMessageInDialogue(dialogue)?.let { getMessageRepresentation(it) }
  }

  fun messagesInDialogue(dialogue: DialogueEntity): List<MessageEntity> {
    return messageRepository.findByDialogueIDOrderByTimestampDesc(dialogue.safeId())
  }

  fun unreadMessagesInDialogue(dialogue: DialogueEntity): List<MessageEntity> {
    return messageRepository.findByDialogueIDAndWasRead(dialogue.safeId(), false)
  }

  fun messagesInDialogueAsJson(dialogue: DialogueEntity): List<MessageDto> {
    return messagesInDialogue(dialogue).map { getMessageRepresentation(it) }
  }

  fun getUnreadMessagesCount(dialogue: DialogueEntity): Int {
    return messageRepository.findByDialogueIDAndWasRead(dialogue.safeId(), false).size
  }

  fun sendMessage(author: User, dialogue: DialogueEntity, body: String): MessageEntity {
    val message = MessageEntity(0, author.safeId(), dialogue.safeId(), body, Instant.now().toEpochMilli())
    messageRepository.save(message)
    return message
  }

  fun getMessageRepresentation(msg: MessageEntity): MessageDto {
    val author = usersService.findUserById(msg.author)
    return MessageDto(
        msg.safeId(),
        author.username,
        author.safeId(),
        msg.dialogueID,
        msg.body,
        msg.timestamp,
        msg.wasRead)
  }

  fun markMessagesInDialogueAsRead(unreadMessages: List<MessageEntity>) {
    unreadMessages
        .forEach {
          it.wasRead = true
          messageRepository.save(it)
        }
  }

  fun markMessageAsRead(msg: MessageEntity) {
    msg.wasRead = true
    messageRepository.save(msg)
  }
}