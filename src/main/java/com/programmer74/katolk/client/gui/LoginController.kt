package com.programmer74.katolk.client.gui

import com.programmer74.katolk.client.feign.FeignRepository
import com.programmer74.katolk.common.data.User
import feign.FeignException
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Stage

class LoginController {

  @FXML
  lateinit var txtUrl: TextField

  @FXML
  lateinit var txtUsername: TextField

  @FXML
  lateinit var txtPassword: PasswordField

  @FXML
  lateinit var cmdGo: Button

  private var stage: Stage? = null
  fun setStage(stage: Stage) {
    this.stage = stage
  }

  @FXML
  fun cmdGoClick(event: ActionEvent) {

    val url = txtUrl.text
    val username = txtUsername.text
    val password = txtPassword.text

    val feignRepo = FeignRepository(url, username, password)
    val userClient = feignRepo.getUserClient()

    val me: User
    try {
      me = userClient.me()
    } catch (fex: FeignException) {
      MessageBoxes.showAlert("Error", fex.localizedMessage)
      return
    }

    try {
      val fxmlFile = "/fxml/main.fxml"
      val loader = FXMLLoader()
      val root = loader.load<Any>(javaClass.getResourceAsStream(fxmlFile)) as Parent
      stage!!.title = "Katolk Client"
      stage!!.scene = Scene(root)
      stage!!.show()
      val controller = loader.getController<Any>() as MainController
      controller.feignRepository = feignRepo
      controller.performPostConstruct()
    } catch (ex: Exception) {
      System.err.println(ex.toString())
    }
  }
}