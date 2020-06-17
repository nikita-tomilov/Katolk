package com.programmer74.katolk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.programmer74.katolk.ws.WsClient
import com.programmer74.katolk.dto.DialogueDto
import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.dto.MessageRequestDto
import com.programmer74.katolk.dto.UserInfoDto
import com.programmer74.katolk.ws.NotificationType
import com.programmer74.katolk.ws.WebsocketNotificationPayload
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.BiConsumer
import java.util.function.Consumer

class KatolkModel(
  private val feignRepository: FeignRepository
) {

  private val dialogueAPI = feignRepository.getDialogueClient()

  private val userAPI = feignRepository.getUserClient()

  private val mapper = ObjectMapper().registerKotlinModule()

  private val wsClient = WsClient(
      "${feignRepository.url}/api/ws/websocket",
      feignRepository.getAccessToken())

  private val executor = Executors.newSingleThreadExecutor()

  var onDialogListRetrievedCallback: Consumer<List<DialogueDto>> = Consumer { }

  var onMessagesRetrievedCallback: BiConsumer<DialogueDto, List<MessageDto>> =
      BiConsumer { _: DialogueDto, _: List<MessageDto> -> }

  var onNewMessageCallback: Consumer<Long> = Consumer { }

  var onUserStateChangedCallback: Consumer<Long> = Consumer { }

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
    onDialogListRetrievedCallback.accept(dialogsList)
  }

  fun scheduleDialogueListUpdate(): Future<*> {
    return executor.submit {
      updateDialoguesList()
    }
  }

  private fun updateDialogueMessages(dialogID: Long) {
    val messages = dialogueAPI.messagesInDialogue(dialogID)
    val dialogue = dialogueAPI.getDialogs().single { it.id == dialogID }
    onMessagesRetrievedCallback.accept(dialogue, messages)
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

  fun getUserInfo(userID: Long, callback: Consumer<UserInfoDto>): Future<*> {
    return executor.submit {
      val user = userAPI.getUser(userID)
      callback.accept(user)
    }
  }

  private fun websocketStringMessageHandler(msg: String) {
   if (msg == "AUTH_OK") {
      executor.submit {
        val me = userAPI.me()
        onAuthOKCallback.accept(me)
      }
    } else {
      try {
        val notification = mapper.readValue<WebsocketNotificationPayload>(msg)
        when (notification.type) {
          NotificationType.NEW_MESSAGE -> {
            val dialogueID = notification.payload["dialogueID"] as Number? ?: return
            onNewMessageCallback.accept(dialogueID.toLong())
          }
          NotificationType.USER_STATE_CHANGED -> {
            val userID = notification.payload["userID"] as Number? ?: return
            onUserStateChangedCallback.accept(userID.toLong())
          }
        }
      } catch (e: Exception) {
        println("Unable to parse message $msg")
        e.printStackTrace()
      }
    }
  }
}