package com.programmer74.katolk.client

import com.programmer74.katolk.FeignRepository
import com.programmer74.katolk.client.ws.WsClient
import com.programmer74.katolk.dto.DialogueDto
import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.dto.MessageRequestDto
import com.programmer74.katolk.dto.UserInfoDto
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.BiConsumer
import java.util.function.Consumer

class KatolkModel(
  private val feignRepository: FeignRepository
) {

  private val dialogueAPI = feignRepository.getDialogueClient()

  private val userAPI = feignRepository.getUserClient()

  private val wsClient = WsClient(
      feignRepository.username,
      feignRepository.password,
      "${feignRepository.url}/api/ws/websocket",
      feignRepository.getToken())

  private val executor = Executors.newSingleThreadExecutor()

  var dialogListConsumer: Consumer<List<DialogueDto>> = Consumer { }

  var messagesConsumer: BiConsumer<DialogueDto, List<MessageDto>> =
      BiConsumer { _: DialogueDto, _: List<MessageDto> -> }

  private var onNewMessageCallback: Runnable = Runnable { }

  private var onAuthOKCallback: Consumer<UserInfoDto> = Consumer {  }

  fun setup(onAuthOKCallback: Consumer<UserInfoDto>) {
    this.onAuthOKCallback = onAuthOKCallback
    assert(feignRepository.isTokenObtained())
    wsClient.open()
    wsClient.add(Consumer { t ->
      websocketStringMessageHandler(t)
    })
  }

  fun getWsClient() = wsClient

  private fun updateDialoguesList() {
    val dialogsList = dialogueAPI.getDialogs()
    dialogListConsumer.accept(dialogsList)
  }

  fun scheduleDialogueListUpdate(): Future<*> {
    return executor.submit {
      updateDialoguesList()
    }
  }

  private fun updateDialogueMessages(dialogID: Long) {
    val messages = dialogueAPI.messagesInDialogue(dialogID)
    val dialogue = dialogueAPI.getDialogs().single { it.id == dialogID }
    messagesConsumer.accept(dialogue, messages)
  }

  fun scheduleRetrievingMessages(dialogueID: Long): Future<*> {
    return executor.submit {
      updateDialogueMessages(dialogueID)
    }
  }

  fun markMessagesAsRead(dialogueID: Long): Future<*> {
    return executor.submit {
      dialogueAPI.markMessagesInDialogueAsRead(dialogueID)
      updateDialogueMessages(dialogueID)
    }
  }

  fun sendMessage(message: MessageRequestDto): Future<*> {
    return executor.submit {
      dialogueAPI.sendMessage(message)
      updateDialogueMessages(message.dialogueID)
      updateDialoguesList()
    }
  }

  fun subscribeOnNewMessage(callback: Runnable) {
    onNewMessageCallback = callback
  }

  fun getUserInfo(userID: Long, callback: Consumer<UserInfoDto>): Future<*> {
    return executor.submit {
      val user = userAPI.getUser(userID)
      callback.accept(user)
    }
  }

  private fun websocketStringMessageHandler(msg: String) {
    if (msg == "UPDATE") {
      onNewMessageCallback.run()
    } else if (msg == "AUTH_OK") {
      executor.submit {
        val me = userAPI.me()
        onAuthOKCallback.accept(me)
      }
    }
  }
}