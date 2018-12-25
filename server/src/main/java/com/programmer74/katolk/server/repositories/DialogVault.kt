package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.server.entity.*
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class DialogVault(val dialogs: DialogueRepository,
                  val participants: DialogueParticipantRepository,
                  val users: UserVault,
                  val messages: MessageVault) {

  fun createDialogue(creator: UserEntity, name: String): DialogueEntity {
    var dialogue = DialogueEntity(0, creator.id, name)
    dialogue = dialogs.save(dialogue)
    val participant = DialogueParticipantEntity(0, dialogue.id,
        creator.id)
    participants.save(participant)
    return dialogue
  }

  fun createDialogue(creator: UserEntity, second: UserEntity): DialogueEntity {
    var dialogue = createDialogue(creator, "PM with ${second.username}")
    addParticipant(dialogue, second)
    return dialogue
  }

  fun addParticipant(dialogue: DialogueEntity, user: UserEntity): DialogueParticipantEntity {
    val participant = DialogueParticipantEntity(0, dialogue.id,
        user.id)
    return participants.save(participant)
  }

  fun getDialogs(user: UserEntity): List<DialogueEntity> {
    val participatesIn = participants.findAllByUserID(user.id)!!.map { it.dialogueID }
    return dialogs.findByIdIn(participatesIn)!!
  }

  fun getParticipants(dialogue: DialogueEntity) : List<UserEntity> {
    val participants = participants.findAllByDialogueID(dialogue.id)!!
    return users.repository.findAllById(participants.map { it.userID }.toList()).toList()
  }

  fun getDialogRepresentations(user: UserEntity): List<DialogueJson> {
    val belongsToDialogsIDs = participants.findAllByUserID(user.id)!!.map { it.dialogueID }
    val belongsToDialogs = dialogs.findByIdIn(belongsToDialogsIDs)

    return belongsToDialogs!!.map {
      DialogueJson(
          it.id,
          it.creator,
          it.name,
          participants.findAllByDialogueID(it.id)!!
              .map { participant -> participant.userID }
              .map { userId -> UserJson.from(users.repository.findById(userId).get()) }
              .toList(),
          messages.latestMessageInDialogueAsJson(it),
          messages.messages.findByDialogueIDAndWasRead(it.id, false).size)
    }.sortedByDescending {
      if (it.latestMessage != null) it.latestMessage.date else 0
    }
  }

  @PostConstruct
  fun initUsers() {
    val admin = users.repository.findByUsername("admin")!!
    val user1 = users.repository.findByUsername("user1")!!
    val user2 = users.repository.findByUsername("user2")!!

    val dialogue = createDialogue(user1, user2)

    val conference = createDialogue(admin, "conference")
    addParticipant(conference, user1)
    addParticipant(conference, user2)

    messages.sendMessage(admin, conference, "Broadcast to users1&2")
    messages.sendMessage(user1, conference, "Reply from user1")
  }
}