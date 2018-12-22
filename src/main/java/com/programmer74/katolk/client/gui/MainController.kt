package com.programmer74.katolk.client.gui

import com.programmer74.katolk.client.feign.FeignRepository
import com.programmer74.katolk.client.feign.UserClient
import com.programmer74.katolk.common.data.User
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextArea
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
  @FXML lateinit var lvDialogs: ListView<*>
  @FXML lateinit var paneMain: BorderPane
  @FXML lateinit var tfUser: TextFlow
  @FXML lateinit var tfMe: TextFlow

  lateinit var feignRepository: FeignRepository
  lateinit var userClient: UserClient
  lateinit var me: User

  fun performPostConstruct() {
    userClient = feignRepository.getUserClient()
    me = userClient.me()
    updateUserInfo(tfMe, me)
  }

  fun updateUserInfo(tf: TextFlow, user: User) {
    val login = Text(user.username + "\n")
    login.font = Font.font("Helvetica", 24.0)

    tf.children.clear()
    tf.children.addAll(login)
  }

  @FXML fun cmdSendClick(event: ActionEvent) {

  }
}