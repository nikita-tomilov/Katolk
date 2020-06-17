package com.programmer74.katolk.service

import com.programmer74.katolk.dao.*
import com.programmer74.katolk.dto.DialogueDto
import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.dto.MessageRequestDto
import com.programmer74.katolk.dto.UserInfoDto
import com.programmer74.katolk.repository.DialogueParticipantRepository
import com.programmer74.katolk.repository.DialogueRepository
import com.programmer74.katolk.ws.WebsocketHandler
import javassist.NotFoundException
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class DialogueService(
  private val dialogueRepository: DialogueRepository,
  private val dialogueParticipantRepository: DialogueParticipantRepository,
  private val userService: UserService,
  private val messagesService: MessagesService
) {

  @Autowired
  private lateinit var websocketHandler: WebsocketHandler

  fun createDialogue(creator: User, name: String): DialogueEntity {
    var dialogue = DialogueEntity(0, creator.safeId(), name)
    dialogue = dialogueRepository.save(dialogue)
    val participant = DialogueParticipantEntity(0, dialogue.safeId(), creator.safeId())
    dialogueParticipantRepository.save(participant)
    return dialogue
  }

  fun createDialogue(creator: User, second: User): DialogueEntity {
    val dialogue = createDialogue(creator, "PM with ${second.username}")
    addParticipant(dialogue, second)
    return dialogue
  }

  fun addParticipant(dialogue: DialogueEntity, user: User): DialogueParticipantEntity {
    val participant = DialogueParticipantEntity(0, dialogue.safeId(), user.safeId())
    return dialogueParticipantRepository.save(participant)
  }

  fun getDialogs(user: User): List<DialogueEntity> {
    val participatesIn =
        dialogueParticipantRepository.findAllByUserID(user.safeId())!!.map { it.dialogueID }
    return dialogueRepository.findByIdIn(participatesIn)!!
  }

  fun getParticipants(dialogue: DialogueEntity): List<User> {
    val participants =
        dialogueParticipantRepository.findAllByDialogueID(dialogue.safeId()) ?: return emptyList()
    return userService.findUsersByIds(participants.map { it.userID }.toList()).toList()
  }

  fun getDialogRepresentations(user: User): List<DialogueDto> {
    val belongsToDialogsIDs =
        dialogueParticipantRepository.findAllByUserID(user.safeId())?.map { it.dialogueID }
          ?: return emptyList()
    val belongsToDialogs = dialogueRepository.findByIdIn(belongsToDialogsIDs)

    return belongsToDialogs!!.map {
      DialogueDto(
          it.safeId(),
          it.creatorId,
          it.name,
          dialogueParticipantRepository.findAllByDialogueID(it.safeId())!!
              .map { participant -> participant.userID }
              .map { userId -> userInfoDtoFromUser(userService.findUserById(userId)) }
              .toList(),
          messagesService.latestMessageInDialogueAsJson(it),
          messagesService.getUnreadMessagesCount(it))
    }.sortedByDescending {
      if (it.latestMessage != null) it.latestMessage?.date else 0
    }
  }

  private fun getDialogueOrNull(userId: Long, dialogueId: Long): DialogueEntity? {
    val participants = dialogueParticipantRepository.findAllByDialogueID(dialogueId)!!
    if (participants.none { it.userID == userId }) {
      return null
    }
    return dialogueRepository.findByIdOrNull(dialogueId)
  }

  fun getMessagesInDialogueAsDto(userId: Long, dialogueId: Long): List<MessageDto> {
    val dialogue = getDialogueOrNull(userId, dialogueId) ?: return emptyList()
    return messagesService.messagesInDialogueAsJson(dialogue)
  }

  fun markMessagesInDialogueAsRead(userId: Long, dialogueId: Long) {
    val dialogue = getDialogueOrNull(userId, dialogueId) ?: return
    val unreadMessages = messagesService.unreadMessagesInDialogue(dialogue)
    if (unreadMessages.isNotEmpty()) {
      messagesService.markMessagesInDialogueAsRead(unreadMessages)
      val participants = getDialogueParticipants(dialogueId)
      participants.forEach { websocketHandler.notifyUserAboutNewMessage(dialogueId, it) }
    }
  }

  private fun sendMessage(user: User, message: MessageRequestDto): MessageEntity {
    val dialogueId = message.dialogueID
    val dialogue = getDialogueOrNull(user.safeId(), dialogueId)
      ?: throw NotFoundException("no dialogue or no access to $dialogueId")

    val sentMessage = messagesService.sendMessage(user, dialogue, message.body)
    val participants = getDialogueParticipants(dialogueId)
    participants.forEach { websocketHandler.notifyUserAboutNewMessage(dialogueId, it) }
    return sentMessage
  }

  fun sendMessageAndReturnDto(user: User, message: MessageRequestDto): MessageDto {
    return sendMessage(user, message).toDto(user.username)
  }

  fun getDialogueParticipants(dialogueId: Long): List<User> {
    val participants =
        dialogueParticipantRepository.findAllByDialogueID(dialogueId) ?: return emptyList()
    val userIds = participants.map { it.userID }
    return userService.findUsersByIds(userIds)
  }

  @PostConstruct
  private fun initDialogues() {
    if (dialogueRepository.findAll().toList().isNotEmpty()) return

    logger.warn { "Performing MANUAL SOFTWARE MIGRATION" }

    val admin = userService.loadUserByUsernameOrNull("admin") ?: return
    val user1 = userService.loadUserByUsernameOrNull("user1") ?: return
    val user2 = userService.loadUserByUsernameOrNull("user2") ?: return

    val dialogue = createDialogue(user1, user2)

    val conference = createDialogue(admin, "conference")
    addParticipant(conference, user1)
    addParticipant(conference, user2)

    val talkToUrself = createDialogue(user1, "Private chat")

    messagesService.sendMessage(admin, conference, "Broadcast to users1&2")
    messagesService.sendMessage(user1, conference, "Reply from user1")
    val msg =
        messagesService.sendMessage(
            user1,
            talkToUrself,
            "This is your private dialogue with yourself")
    messagesService.markMessageAsRead(msg)
  }

  companion object : KLogging()
}