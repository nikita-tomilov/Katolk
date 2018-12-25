package com.programmer74.katolk.server.repositories


import com.programmer74.katolk.server.entity.DialogueEntity
import com.programmer74.katolk.server.entity.DialogueParticipantEntity
import com.programmer74.katolk.server.entity.MessageEntity
import com.programmer74.katolk.server.entity.UserEntity
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<UserEntity, Int> {
  fun findByUsername(username: String): UserEntity?
}

interface MessageRepository : CrudRepository<MessageEntity, Int> {
  fun findFirstByDialogueIDOrderByDateDesc(dialogueID: Int): MessageEntity?
  fun findByDialogueIDOrderByDateDesc(dialogueID: Int): List<MessageEntity>
  fun findByDialogueIDAndWasRead(dialogueID: Int, wasRead: Boolean): List<MessageEntity>
}

interface DialogueRepository : CrudRepository<DialogueEntity, Int> {
  fun findByIdIn(id: List<Int>): List<DialogueEntity>?
}

interface DialogueParticipantRepository : CrudRepository<DialogueParticipantEntity, Int> {
  fun findAllByUserID(userID: Int): List<DialogueParticipantEntity>?
  fun findAllByDialogueID(dialogueID: Int): List<DialogueParticipantEntity>?
}