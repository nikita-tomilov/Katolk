package com.programmer74.katolk.ws

import com.programmer74.katolk.dao.User
import com.programmer74.katolk.dto.ClientBinaryMessage
import com.programmer74.katolk.dto.ClientBinaryMessageType
import com.programmer74.katolk.dto.OnlineUserStatus
import com.programmer74.katolk.service.TalkService
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class BinaryMessageHandler(
  private val talkService: TalkService
) {

  fun handleBinaryMessagingLogic(
    clientRequest: ClientBinaryMessage,
    session: WebSocketSession,
    receivedFrom: User
  ) {
    when {
      clientRequest.type == ClientBinaryMessageType.PING_SERVER_REQUEST -> {
        val clientResponse = ClientBinaryMessage(
            ClientBinaryMessageType.PING_SERVER_RESPONSE,
            clientRequest.payload, receivedFrom)
        session.secureSendMessage(clientResponse)
      }
      clientRequest.type == ClientBinaryMessageType.CALL_REQUEST -> {
        handleCallRequest(clientRequest, session, receivedFrom)
      }
      clientRequest.type == ClientBinaryMessageType.CALL_RESPONSE_ALLOW ||
          clientRequest.type == ClientBinaryMessageType.CALL_RESPONSE_DENY -> {
        handleCallResponse(clientRequest, session, receivedFrom)
      }
      clientRequest.type == ClientBinaryMessageType.CALL_END -> {
        handleTalkEndNormally(session, receivedFrom)
      }
      else -> handleForwarding(clientRequest, session)
    }
  }

  fun handleUserDisconnected(session: WebSocketSession, user: User) {
    handleTalkEndAbnormally(session, user)
  }

  //TODO: handle IDs being LONG, not INT
  private fun handleCallRequest(
    clientRequest: ClientBinaryMessage,
    session: WebSocketSession,
    requestFrom: User
  ) {
    val requestedUser = talkService.getOnlineUser(clientRequest.intPayload().toLong())
    if (requestedUser == null) {
      session.secureSendMessage(ClientBinaryMessage.callErrorUserNotFound(requestFrom))
      return
    }

    val requestedOnlineUser = talkService.getOnlineSession(requestedUser)
    if (requestedOnlineUser == null) {
      session.secureSendMessage(ClientBinaryMessage.callErrorOffline(requestFrom))
      return
    }

    val requestedUserStatus = talkService.getUserStatus(requestedUser)
    if (requestedUserStatus != OnlineUserStatus.READY_FOR_CALL) {
      if (requestFrom != requestedUser) {
        session.secureSendMessage(
            ClientBinaryMessage.callErrorUserBusy(
                requestFrom,
                requestedUserStatus))
        return
      }
    }

    val forwardedRequest = ClientBinaryMessage(
        ClientBinaryMessageType.CALL_REQUEST,
        requestFrom.safeId().toInt(), requestFrom)
    val forwardTo =
        talkService.getOnlineSession(requestedUser) ?: error("should-never-happen")
    forwardTo.secureSendMessage(forwardedRequest)

    talkService.setUserStatus(requestFrom, OnlineUserStatus.CALLING)
    if (requestFrom != requestedUser) {
      talkService.setUserStatus(requestedUser, OnlineUserStatus.BEING_CALLED)
    }
  }

  //TODO: handle IDs being LONG, not INT
  @Suppress("UnnecessaryVariable")
  private fun handleCallResponse(
    clientRequest: ClientBinaryMessage,
    session: WebSocketSession,
    callReceiver: User
  ) {
    val callInitiator = talkService.getOnlineUser(clientRequest.intPayload().toLong())
    if (callInitiator == null) {
      session.secureSendMessage(ClientBinaryMessage.callErrorUserNotFound(callReceiver))
      return
    }

    val callInitiatorStatus = talkService.getUserStatus(callInitiator)
    if (callInitiatorStatus != OnlineUserStatus.CALLING) {
      session.secureSendMessage(
          ClientBinaryMessage.callErrorUserBusy(
              callReceiver,
              callInitiatorStatus))
      return
    }

    if (callInitiator != callReceiver) {
      val callReceiverStatus = talkService.getUserStatus(callReceiver)
      if (callReceiverStatus != OnlineUserStatus.BEING_CALLED) {
        session.secureSendMessage(
            ClientBinaryMessage.callErrorUserBusy(
                callInitiator,
                callReceiverStatus))
        return
      }
    }

    val callInitiatorSession =
        talkService.getOnlineSession(callInitiator) ?: error("should-never-happen")

    val response = ClientBinaryMessage(
        clientRequest.type,
        callReceiver.safeId().toInt(), callReceiver)
    callInitiatorSession.secureSendMessage(response)

    if (clientRequest.type == ClientBinaryMessageType.CALL_RESPONSE_ALLOW) {
      val callReceiverSession =
          talkService.getOnlineSession(callReceiver) ?: error("should-never-happen")
      talkService.addTalk(callInitiatorSession, callReceiverSession)

      callReceiverSession.secureSendMessage(
          ClientBinaryMessage(
              ClientBinaryMessageType.CALL_BEGIN,
              callInitiator.safeId().toInt(),
              callReceiver)
      )
      if (callInitiator != callReceiver) {
        callInitiatorSession.secureSendMessage(
            ClientBinaryMessage(
                ClientBinaryMessageType.CALL_BEGIN,
                callReceiver.safeId().toInt(),
                callReceiver)
        )
      }
    } else {
      talkService.setUserStatus(callReceiver, OnlineUserStatus.READY_FOR_CALL)
    }
  }

  private fun handleTalkEndAbnormally(session: WebSocketSession, requestFrom: User) {
    val talk = talkService.getTalk(session) ?: return
    val notifyTo =
        if (talk.key == session) talk.value else talk.key
    val msg =
        ClientBinaryMessage(
            ClientBinaryMessageType.CALL_END_ABNORMAL,
            ByteArray(0),
            requestFrom)
    notifyTo.secureSendMessage(msg)
    talkService.removeTalk(notifyTo)
  }

  private fun handleTalkEndNormally(session: WebSocketSession, requestFrom: User) {
    logger.warn { "handleTalkEndNormally on user '$requestFrom'" }
    val talk = talkService.getTalk(session) ?: return
    val notifyTo =
        if (talk.key == session) talk.value else talk.key
    logger.warn { "talk $talk" }
    val msg =
        ClientBinaryMessage(
            ClientBinaryMessageType.CALL_END,
            ByteArray(0),
            requestFrom)

    val oneParticipant = talkService.getOnlineUser(session)
    val anotherParticipant = talkService.getOnlineUser(notifyTo)

    session.secureSendMessage(msg)
    logger.warn { "sent CALL_END to user '$oneParticipant'" }
    if (session != notifyTo) {
      logger.warn { "sent CALL_END to user '$anotherParticipant'" }
      notifyTo.secureSendMessage(msg)
    }
    talkService.removeTalk(notifyTo)
  }

  private fun handleForwarding(message: ClientBinaryMessage, session: WebSocketSession) {
    val talk = talkService.getTalk(session) ?: return
    val forwardTo =
        if (talk.key == session) talk.value else talk.key

    forwardTo.secureSendMessage(message)
  }

  companion object : KLogging()
}