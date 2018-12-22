package com.programmer74.katolk.client.gui

import com.google.common.collect.Lists
import com.programmer74.katolk.client.feign.DialogueClient
import com.programmer74.katolk.client.feign.FeignRepository
import com.programmer74.katolk.client.feign.UserClient
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

  lateinit var me: User
  var selectedDialogue: DialogueJson? = null

  fun performPostConstruct() {
    userClient = feignRepository.getUserClient()
    dialogueClient = feignRepository.getDialogueClient()
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

    var from: String
    var msg: String

    val historySb = StringBuilder()

    historySb.append("<html>")
    historySb.append("<head>")
    historySb.append("   <script language=\"javascript\" type=\"text/javascript\">")
    historySb.append("       function toBottom(){")
    historySb.append("           window.scrollTo(0, document.body.scrollHeight);")
    historySb.append("       }")
    historySb.append("   </script>")
    historySb.append("<style>")
    historySb.append("body {\n" +
        "    background-color: white;\n" +
        "    color: white;\n" +
        "    font-family: Arial, Helvetica, sans-serif;\n" +
        "    font-size: 11pt;\n" +
        "}\n" +
        "div {\n" +
        "\tmax-width: 70%;\n" +
        "    clear:both;\n" +
        "    padding: 10px; \n" +
        "}\n" +
        ".my {\n" +
        "\tbackground-color: green;\n" +
        "    text-align: left;\n" +
        "    float:right;\n" +
        "    border-radius: 25px 25px 5px 25px;\n" +
        "}\n" +
        ".his {\n" +
        "\tbackground-color: darkgreen;\n" +
        "    text-align: left;\n" +
        "    float:left;\n" +
        "    border-radius: 25px 25px 25px 5px;\n" +
        "}")
    historySb.append("</style>")
    historySb.append("</head>")
    historySb.append("<body onload='toBottom()'>")

    var prevUsername = ""
    var shouldIncludeUsername: Boolean

    for (message in Lists.reverse(messages)) {

      from = message.author
      shouldIncludeUsername = false
      if (from != prevUsername) {
        prevUsername = from
        shouldIncludeUsername = true
      }
      msg = message.body

      historySb.append("<div class=\"" + (if (message.authorId == me.id) "my" else "his") + "\">")

      msg = msg.replace("[<]".toRegex(), "&lt;")
      msg = msg.replace("[>]".toRegex(), "&gt;")
      msg = msg.replace("\n|\r\n|\n\r|\r".toRegex(), "<br>")

      if (shouldIncludeUsername) {
        historySb.append("<b>").append(from).append("</b><br>")
      }
      historySb.append(msg)

      historySb.append("</div>\n")
    }
    historySb.append("</body>")
    historySb.append("</html>")
    Platform.runLater { wvMessageHistory.engine.loadContent(historySb.toString()) }
  }

  @FXML fun cmdSendClick(event: ActionEvent) {
    val dialogueCopy = selectedDialogue ?: return

    val msg = Message(0, 0, dialogueCopy.id, txtMessage.text, 0)
    dialogueClient.sendMessage(msg)
    uiUpdateDialogueList()
    uiUpdateMessagesView()
  }
}