package com.programmer74.katolk.client.gui

import com.google.common.collect.Lists
import com.programmer74.katolk.client.feign.DialogueClient
import com.programmer74.katolk.client.feign.FeignRepository
import com.programmer74.katolk.client.feign.UserClient
import com.programmer74.katolk.client.feign.WsClient
import com.programmer74.katolk.common.data.DialogueJson
import com.programmer74.katolk.common.data.Message
import com.programmer74.katolk.common.data.User
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView
import javafx.scene.text.TextFlow
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.web.WebView
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

  lateinit var me: User
  var selectedDialogue: DialogueJson? = null

  fun performPostConstruct() {
    userClient = feignRepository.getUserClient()
    dialogueClient = feignRepository.getDialogueClient()
    wsClient = feignRepository.getWsClient()
    wsClient.open()
    wsClient.add(Consumer { t ->
      if (t == "UPDATE") {
        uiUpdateDialogueList()
        uiUpdateMessagesView()
      }
    })
    me = userClient.me()
    uiUpdateUserInfo(tfMe, me)
    uiUpdateDialogueList()
  }

  fun uiUpdateUserInfo(tf: TextFlow, user: User) {
    val login = Text(user.username + "\n")
    login.font = Font.font("Helvetica", 24.0)

    tf.children.clear()
    tf.children.addAll(login)
  }

  fun uiUpdateDialogueList() {
    val dialogs = dialogueClient.getDialogs()

    val dialogsAtLw = FXCollections.observableArrayList<DialogueJson>()
    dialogs.forEach { dialogsAtLw.add(it) }

    lvDialogs.setCellFactory { param ->
      object : ListCell<DialogueJson>() {
        private val imageView = ImageView()
        public override fun updateItem(dialogue: DialogueJson?, empty: Boolean) {
          if (dialogue == null) {
            return
          }
          super.updateItem(dialogue, empty)
          if (empty) {
            text = null
            graphic = null
          } else {
            if (dialogue.participants.size == 2) {
              text = dialogue.participants.first { it != me.username }
            } else {
              text = "You and ${dialogue.participants.size - 1} more"
            }
            graphic = imageView
            onMouseClicked = EventHandler { lvDialogsDialogClicked() }
          }
        }
      }
    }
    lvDialogs.items = dialogsAtLw
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

    Platform.runLater { wvMessageHistory.engine.loadContent(html) }
  }

  @FXML fun cmdSendClick(event: ActionEvent) {
    val dialogueCopy = selectedDialogue ?: return

    val msg = Message(0, 0, dialogueCopy.id, txtMessage.text, 0)
    dialogueClient.sendMessage(msg)
    uiUpdateDialogueList()
    uiUpdateMessagesView()
  }
}