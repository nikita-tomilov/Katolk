package com.programmer74.katolk.client.gui

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
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BorderPane
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

  @FXML lateinit var paneCenter: BorderPane
  @FXML lateinit var txtMessage: TextArea
  @FXML lateinit var wvMessageHistory: WebView
  @FXML lateinit var cmdSend: Button
  @FXML lateinit var lvDialogs: ListView<DialogueJson>
  @FXML lateinit var paneMain: BorderPane
  @FXML lateinit var tfUser: TextFlow
  @FXML lateinit var tfMe: TextFlow

  lateinit var feignRepository: FeignRepository
  lateinit var userClient: UserClient
  lateinit var dialogueClient: DialogueClient
  lateinit var wsClient: WsClient

  lateinit var me: UserJson
  var selectedDialogue: DialogueJson? = null

  lateinit var onlineImage: javafx.scene.image.Image
  lateinit var offlineImage: javafx.scene.image.Image
  lateinit var conferenceImage: javafx.scene.image.Image

  fun performPostConstruct() {
    userClient = feignRepository.getUserClient()
    dialogueClient = feignRepository.getDialogueClient()
    wsClient = feignRepository.getWsClient()
    wsClient.open()
    wsClient.add(Consumer { t ->
      if (t == "UPDATE") {
        Platform.runLater {
          uiUpdateDialogueList()
          uiUpdateMessagesView()
        }
      }
    })
    me = userClient.me()
    uiSetupDialogListImages()
    uiUpdateUserInfo(tfMe, me)
    uiUpdateDialogueList()
    txtMessage.setOnKeyPressed {
      if ((it.code == KeyCode.ENTER) && !(it.isShiftDown) && !(it.isControlDown)) {
        cmdSendClick(ActionEvent())
        it.consume()
      }
    }
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

            val dialogueNameV = Text(dialogueName + "\n")
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
  }

  private fun uiUpdateMessagesView() {
    val dialogueCopy = selectedDialogue

    if (dialogueCopy == null) {
      wvMessageHistory.engine.loadContent("<html></html>")
      return
    }

    val messages = dialogueClient.getMessages(dialogueCopy.id)

    val html = buildHTML(messages, me)

    Platform.runLater {
      if (dialogueCopy.participants.size == 2) {
        uiUpdateUserInfo(tfUser, dialogueCopy.participants.first { it.id != me.id })
      } else {
        uiUpdateUserInfo(tfUser, dialogueCopy)
      }
      wvMessageHistory.engine.loadContent(html)
    }

    if (messages.firstOrNull { (it.wasRead == false) && (it.author != me.username) } != null) {
//      Platform.runLater {
        dialogueClient.markReadMessages(dialogueCopy.id)
//      }
    }
  }

  private fun uiSetupDialogListImages() {
    val img = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    val g = img.graphics

    g.color = Color.GREEN
    g.fillOval(1, 1, 15, 15)
    onlineImage = SwingFXUtils.toFXImage(img, null)

    g.color = Color.GRAY
    g.fillOval(1, 1, 15, 15)
    offlineImage = SwingFXUtils.toFXImage(img, null)

    g.color = Color.BLUE
    g.fillOval(1, 1, 15, 15)
    conferenceImage = SwingFXUtils.toFXImage(img, null)
  }

  @FXML fun cmdSendClick(event: ActionEvent) {
    val dialogueCopy = selectedDialogue ?: return

    val msg = Message(0, 0, dialogueCopy.id, txtMessage.text, 0)
    dialogueClient.sendMessage(msg)
    uiUpdateDialogueList()
    uiUpdateMessagesView()
    txtMessage.text = ""
  }
}