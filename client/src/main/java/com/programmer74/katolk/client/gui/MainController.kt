package com.programmer74.katolk.client.gui

import com.programmer74.katolk.client.audio.Audio
import com.programmer74.katolk.client.binary.BinaryMessage
import com.programmer74.katolk.client.binary.BinaryMessageType
import com.programmer74.katolk.client.data.DialogueJson
import com.programmer74.katolk.client.data.Message
import com.programmer74.katolk.client.data.MessageJson
import com.programmer74.katolk.client.data.UserJson
import com.programmer74.katolk.client.feign.DialogueClient
import com.programmer74.katolk.client.feign.FeignRepository
import com.programmer74.katolk.client.feign.UserClient
import com.programmer74.katolk.client.feign.WsClient
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.scene.web.WebView
import java.awt.Color
import java.awt.image.BufferedImage
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset
import java.util.function.Consumer

class MainController {

  @FXML
  lateinit var paneCenter: BorderPane
  @FXML
  lateinit var paneAboveDialog: VBox
  @FXML
  lateinit var paneUserInfo: Pane
  @FXML
  lateinit var txtMessage: TextArea
  @FXML
  lateinit var wvMessageHistory: WebView
  @FXML
  lateinit var cmdSend: Button
  @FXML
  lateinit var lvDialogs: ListView<DialogueJson>
  @FXML
  lateinit var paneMain: BorderPane
  @FXML
  lateinit var tfUser: TextFlow
  @FXML
  lateinit var tfMe: TextFlow
  @FXML
  lateinit var mnuBeginCall: MenuItem
  @FXML
  lateinit var mnuEndCall: MenuItem

  @FXML
  lateinit var lblDialogWith: Label
  @FXML
  lateinit var lblTalkWith: Label

  lateinit var feignRepository: FeignRepository
  lateinit var userClient: UserClient
  lateinit var dialogueClient: DialogueClient
  lateinit var wsClient: WsClient
  lateinit var audio: Audio

  lateinit var me: UserJson
  var messagesOpponent: UserJson? = null
  var callOpponent: UserJson? = null
  var selectedDialogue: DialogueJson? = null
  var callInProgress = false
  var callThread: Thread? = null

  lateinit var onlineImage: javafx.scene.image.Image
  lateinit var offlineImage: javafx.scene.image.Image
  lateinit var conferenceImage: javafx.scene.image.Image

  fun performPostConstruct() {
    userClient = feignRepository.getUserClient()
    dialogueClient = feignRepository.getDialogueClient()
    wsClient = feignRepository.getWsClient()
    wsClient.open()
    wsClient.add(Consumer { t ->
      WsStringMsgHandler(t)
    })
    wsClient.addBinary(Consumer { t ->
      WsBinaryMsgHandler(t)
    })
    audio = Audio(3, wsClient)
    me = userClient.me()
    uiSetupDialogListImages()
    uiUpdateUserInfo(tfMe, me)
    uiUpdateDialogueList()
    uiUpdateCallMenus()
    txtMessage.setOnKeyPressed {
      if ((it.code == KeyCode.ENTER) && !(it.isShiftDown) && !(it.isControlDown)) {
        cmdSendClick(ActionEvent())
        it.consume()
      }
    }
    paneUserInfo.isVisible = false
    paneUserInfo.isManaged = false
    paneAboveDialog.isVisible = false
    paneAboveDialog.isManaged = false
    lblTalkWith.isVisible = false
    lblTalkWith.isManaged = false
    lblTalkWith.prefWidthProperty().bind(paneAboveDialog.widthProperty())
    lblDialogWith.prefWidthProperty().bind(paneAboveDialog.widthProperty())
  }

  fun uiUpdateUserInfo(tf: TextFlow, user: UserJson) {
    val login = Text(user.username + "\n")
    login.font = Font.font("Helvetica", 24.0)

    val name = Text(user.name + " " + user.surname + "\n")
    name.font = Font.font("Helvetica", 18.0)

    val birthDate =
        LocalDateTime
            .ofEpochSecond(user.born / 1000, 0, ZoneOffset.UTC)
            .toLocalDate()
    val age = Period.between(birthDate, LocalDate.now()).years

    val ageCity = Text(user.gender + age.toString() + ", " + user.city + "\n")
    ageCity.font = Font.font("Helvetica", 16.0)

    val lastOnlineDate = LocalDateTime
        .ofEpochSecond(user.lastOnline / 1000, 0, ZoneOffset.UTC)
    val lastOnlineText = if (user.online)
      "Online"
    else
      "Last online " + lastOnlineDate.toString()
    val lastOnline = Text(lastOnlineText)
    lastOnline.font = Font.font("Helvetica", 14.0)

    tf.children.clear()
    tf.children.addAll(login, name, ageCity, lastOnline)
  }

  fun uiUpdateUserInfo(tf: TextFlow, dialogue: DialogueJson) {

    tf.children.clear()

    val title = Text(dialogue.name + "\n")
    title.font = Font.font("Helvetica", 24.0)
    tf.children.add(title)

    val logins = dialogue.participants.map { Text(" " + it.username) }
    logins.forEach { it.font = Font.font("Helvetica", 12.0) }

    tf.children.addAll(logins)
  }

  fun uiUpdateDialogueList() {
    val dialogs = dialogueClient.getDialogs()

    val dialogsAtLw = FXCollections.observableArrayList<DialogueJson>()
    dialogs.forEach { dialogsAtLw.add(it) }

    lvDialogs.setCellFactory { param ->
      object : ListCell<DialogueJson>() {
        public override fun updateItem(dialogue: DialogueJson?, empty: Boolean) {
          if (dialogue == null) {
            return
          }
          super.updateItem(dialogue, empty)
          if (empty) {
            text = null
            graphic = null
          } else {
            val dialogueIcon = ImageView()
            val content = TextFlow()
            var dialogueName = ""
            var lastMessage = ""
            if (dialogue.participants.size == 2) {
              val dialogWith = dialogue.participants.first { it.username != me.username }
              dialogueName = dialogWith.username
              if (dialogWith.online) {
                dialogueIcon.image = onlineImage
              } else {
                dialogueIcon.image = offlineImage
              }
            } else {
              dialogueName = dialogue.name
              dialogueIcon.image = conferenceImage
            }
            if (dialogue.latestMessage != null) {
              lastMessage = extractMessagePreview(dialogue.latestMessage)
            }

            val dialogueNameV = Text(" $dialogueName\n")
            dialogueNameV.font = Font("Helvetica", 18.0)

            if (dialogue.unreadCount != 0) {
              lastMessage = "[${dialogue.unreadCount}] $lastMessage"
            }
            val lastMessageV = Text(lastMessage)
            lastMessageV.font = Font("Helvetica", 14.0)

            content.children.addAll(dialogueIcon, dialogueNameV, lastMessageV)
            text = null
            graphic = content
            onMouseClicked = EventHandler { lvDialogsDialogClicked() }
          }
        }
      }
    }
    lvDialogs.items = dialogsAtLw
  }

  fun uiUpdateCallMenus() {
    if (callInProgress) {
      mnuBeginCall.isDisable = true
      mnuEndCall.isDisable = false
      return
    } else {
      mnuEndCall.isDisable = true
    }
    val selectedDialogue = this.selectedDialogue
    val opponent = this.messagesOpponent
    if (selectedDialogue == null || opponent == null) {
      mnuBeginCall.isDisable = true
      return
    }

    mnuBeginCall.isDisable = !opponent.online
  }

  private fun extractMessagePreview(latestMessage: MessageJson): String {
    var latestMessagePreview = latestMessage.body.trim()
    val maxlen = 10
    if (latestMessagePreview.length > maxlen) {
      var i = latestMessagePreview.indexOf(' ', 0)
      while ((i >= 0) && (i < maxlen)) {
        val oldi = i
        i = latestMessagePreview.indexOf(' ', oldi + 1)
        if (i == oldi) break
      }
      if (i == -1) {
        if (latestMessagePreview.length > maxlen) {
          i = maxlen
        }
      }
      latestMessagePreview = latestMessagePreview.substring(0, i) + "..."
    }
    return latestMessage.author + ": " + latestMessagePreview
  }

  private fun lvDialogsDialogClicked() {
    selectedDialogue = lvDialogs.selectionModel.selectedItem
    uiUpdateMessagesView()
    uiUpdateCallMenus()
  }

  private fun uiUpdateMessagesView() {
    val dialogue = selectedDialogue

    if (dialogue == null) {
      wvMessageHistory.engine.loadContent("<html></html>")
      messagesOpponent = null
      lblDialogWith.isVisible = false
      paneAboveDialog.isVisible = lblDialogWith.isVisible || lblTalkWith.isVisible
      paneAboveDialog.isManaged = paneAboveDialog.isVisible
      return
    }

    messagesOpponent = dialogue.getOpponent(me)

    val messages = dialogueClient.getMessages(dialogue.id)

    val html = buildHTML(messages, me)

    Platform.runLater {
      if (dialogue.participants.size == 2) {
        uiUpdateUserInfo(tfUser, messagesOpponent!!)
        lblDialogWith.text = "Dialog with ${messagesOpponent!!.username}"
      } else {
        uiUpdateUserInfo(tfUser, dialogue)
        lblDialogWith.text = "${dialogue.name}"
      }
      wvMessageHistory.engine.loadContent(html)

      lblDialogWith.isVisible = true
      paneAboveDialog.isVisible = true
      paneAboveDialog.isManaged = paneAboveDialog.isVisible
    }

    val unreadMessages = messages.filter { !it.wasRead }
    if (unreadMessages.isNotEmpty()) {
      val notMineMessages = unreadMessages.filter { it.authorId != me.id }
      if (notMineMessages.isNotEmpty() || (dialogue.participants.size == 1)) {
        dialogueClient.markReadMessages(dialogue.id)
      }
    }
  }

  private fun uiSetupDialogListImages() {
    val img = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    val g = img.graphics

    g.color = Color.decode("0x457313")
    g.fillOval(1, 1, 15, 15)
    onlineImage = SwingFXUtils.toFXImage(img, null)

    g.color = Color.decode("0x802D15")
    g.fillOval(1, 1, 15, 15)
    offlineImage = SwingFXUtils.toFXImage(img, null)

    g.color = Color.decode("0x113B51")
    g.fillOval(1, 1, 15, 15)
    conferenceImage = SwingFXUtils.toFXImage(img, null)
  }

  @FXML
  fun cmdSendClick(event: ActionEvent) {
    val dialogue = selectedDialogue ?: return
    val msg = Message(0, 0, dialogue.id, txtMessage.text, 0)
    dialogueClient.sendMessage(msg)
    uiUpdateDialogueList()
    uiUpdateMessagesView()
    txtMessage.text = ""
  }

  @FXML
  fun mnuBeginCallClick(event: ActionEvent) {
    val opponent = this.messagesOpponent ?: return
    val callMessage = BinaryMessage(BinaryMessageType.CALL_REQUEST, opponent.id)
    wsClient.client.send(callMessage.toBytes())
  }

  @FXML
  fun mnuEndCallClick(event: ActionEvent) {
    val opponent = this.callOpponent ?: return
    val callMessage = BinaryMessage(BinaryMessageType.CALL_END, opponent.id)
    wsClient.client.send(callMessage.toBytes())
  }

  @FXML
  fun lblDialogWithClicked(event: MouseEvent) {
    paneUserInfo.isVisible = !paneUserInfo.isVisible
    paneUserInfo.isManaged = paneUserInfo.isVisible
  }

  @FXML
  fun lblTalkWithClicked(event: MouseEvent) {

  }

  fun WsStringMsgHandler(msg: String) {
    if (msg == "UPDATE") {
      Platform.runLater {
        uiUpdateDialogueList()
        uiUpdateMessagesView()
        uiUpdateCallMenus()
      }
    } else if (msg == "AUTH_OK") {
      Platform.runLater {
        me = userClient.me()
        uiUpdateUserInfo(tfMe, me)
      }
    }
  }

  fun WsBinaryMsgHandler(msg: BinaryMessage) {
    when {
      msg.type == BinaryMessageType.CALL_REQUEST -> {
        val asker = userClient.getUser(msg.intPayload())
        val prompt = "Incoming call from ${asker.username}. Accept?"
        val agree = MessageBoxes.showYesNoAlert(prompt)
        val answer =
            if (agree) {
              BinaryMessage(BinaryMessageType.CALL_RESPONSE_ALLOW, asker.id)
            } else {
              BinaryMessage(BinaryMessageType.CALL_RESPONSE_DENY, asker.id)
            }
        wsClient.send(answer)
      }
      msg.type == BinaryMessageType.CALL_BEGIN -> {
        handleCallBegin(userClient.getUser(msg.intPayload()))
      }
      msg.type == BinaryMessageType.CALL_END -> {
        handleCallEnd("Call ended")
      }
      msg.type == BinaryMessageType.CALL_ERROR -> {
        MessageBoxes.showAlert("Error calling", "Error")
      }
      msg.type == BinaryMessageType.CALL_END_ABNORMAL -> {
        handleCallEnd("Abnormal call ending. Probably opponent disconnected")
      }
    }
  }

  fun handleCallBegin(user: UserJson) {
    callInProgress = true
    uiUpdateCallMenus()
    System.err.println("BEGIN CALL")
    lblTalkWith.isVisible = true
    lblTalkWith.isManaged = true
    callOpponent = user
    callThread = Thread(Runnable {
      var time = 0
      while (callInProgress) {
        val mins = "${time / 60}".padStart(2, '0')
        val secs = "${time % 60}".padStart(2, '0')
        Platform.runLater {
          lblTalkWith.text = "Call with ${user.username} $mins:$secs"
        }
        Thread.sleep(1000)
        time++
      }
    })
    callThread!!.start()
    audio.Talk()
    audio.Listen()
    wsClient.isOpponentAvailable = true
  }

  fun handleCallEnd(msg: String) {
    if (!callInProgress) {
      return
    }
    audio.StopListening()
    audio.StopTalking()
    wsClient.isOpponentAvailable = false
    callInProgress = false
    callOpponent = null
    uiUpdateCallMenus()
    MessageBoxes.showAlert(msg, "Info")
    System.err.println("END CALL")
    callThread!!.join()
    callThread = null
    lblTalkWith.isVisible = false
    lblTalkWith.isManaged = false
  }
}