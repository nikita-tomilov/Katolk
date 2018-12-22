package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.common.data.Dialogue
import com.programmer74.katolk.common.data.DialogueParticipant
import com.programmer74.katolk.common.data.Message
import com.programmer74.katolk.common.data.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Int> {
  fun findByUsername(username: String): User?
}

interface MessageRepository : CrudRepository<Message, Int> {
  fun findFirstByDialogueIDOrderByDateDesc(dialogueID: Int): Message?
  fun findByDialogueIDOrderByDateDesc(dialogueID: Int): List<Message>
}

interface DialogueRepository : CrudRepository<Dialogue, Int> {
  fun findByIdIn(id: List<Int>): List<Dialogue>?
}

interface DialogueParticipantRepository : CrudRepository<DialogueParticipant, Int> {
  fun findAllByUserID(userID: Int): List<DialogueParticipant>?
  fun findAllByDialogueID(dialogueID: Int): List<DialogueParticipant>?
}