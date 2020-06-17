package com.programmer74.katolk.repository

import com.programmer74.katolk.dao.DialogueEntity
import com.programmer74.katolk.dao.DialogueParticipantEntity
import com.programmer74.katolk.dao.MessageEntity
import org.springframework.data.repository.CrudRepository

interface MessageRepository : CrudRepository<MessageEntity, Long> {
  fun findFirstByDialogueIDOrderByTimestampDesc(dialogueID: Long): MessageEntity?
  fun findByDialogueIDOrderByTimestampDesc(dialogueID: Long): List<MessageEntity>
  fun findByDialogueIDAndWasRead(dialogueID: Long, wasRead: Boolean): List<MessageEntity>
}

interface DialogueRepository : CrudRepository<DialogueEntity, Long> {
  fun findByIdIn(id: List<Long>): List<DialogueEntity>?
}

interface DialogueParticipantRepository : CrudRepository<DialogueParticipantEntity, Long> {
  fun findAllByUserID(userID: Long): List<DialogueParticipantEntity>?
  fun findAllByDialogueID(dialogueID: Long): List<DialogueParticipantEntity>?
}