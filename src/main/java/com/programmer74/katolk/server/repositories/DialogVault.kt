package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.common.data.Dialogue
import com.programmer74.katolk.common.data.DialogueParticipant
import com.programmer74.katolk.common.data.DialogueJson
import com.programmer74.katolk.common.data.User
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class DialogVault(val dialogs: DialogueRepository,
                  val participants: DialogueParticipantRepository,
                  val users: UserVault,
                  val messages: MessageVault) {

  fun createDialogue(creator: User): Dialogue {
    var dialogue = Dialogue(0, creator.id)
    dialogue = dialogs.save(dialogue)
    val participant = DialogueParticipant(0, dialogue.id, creator.id)
    participants.save(participant)
    return dialogue
  }

  fun addParticipant(dialogue: Dialogue, user: User): DialogueParticipant {
    val participant = DialogueParticipant(0, dialogue.id, user.id)
    return participants.save(participant)
  }

  fun getDialogs(user: User): List<Dialogue> {
    val participatesIn = participants.findAllByUserID(user.id)!!.map { it.dialogueID }
    return dialogs.findByIdIn(participatesIn)!!
  }

  fun getDialogRepresentations(user: User): List<DialogueJson> {
    val belongsToDialogsIDs = participants.findAllByUserID(user.id)!!.map { it.dialogueID }
    val belongsToDialogs = dialogs.findByIdIn(belongsToDialogsIDs)

    return belongsToDialogs!!.map {
      DialogueJson(
          it.id,
          it.creator,
          participants.findAllByDialogueID(it.id)!!
              .map { participant -> participant.userID }
              .map { userId -> users.repository.findById(userId).get().username }
              .toList(),
          messages.latestMessageInDialogueAsJson(it))
    }.toList()
  }

  @PostConstruct
  fun initUsers() {
    val admin = users.repository.findByUsername("admin")!!
    val user1 = users.repository.findByUsername("user1")!!
    val user2 = users.repository.findByUsername("user2")!!

    val dialogue = createDialogue(user1)
    addParticipant(dialogue, user2)

    val conference = createDialogue(admin)
    addParticipant(conference, user1)
    addParticipant(conference, user2)

    messages.sendMessage(admin, conference, "Broadcast to users1&2")
    messages.sendMessage(user1, conference, "Reply from user1")
  }
}