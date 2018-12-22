package com.programmer74.katolk.client.gui

import com.programmer74.katolk.client.feign.FeignRepository
import javafx.event.ActionEvent
import javafx.fxml.FXML
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
    val me = userClient.me()

    if (me.username != username) {
      System.err.println("wtf")
    } else {
      val alert = Alert(Alert.AlertType.ERROR)
      alert.title = "Error"
      alert.headerText = null
      alert.contentText = "WOW FUCK U R $username"
      alert.showAndWait()
      return
    }


    try {
//      val fxmlFile = "/fxml/main.fxml"
//      val loader = FXMLLoader()
//      val root = loader.load<Any>(javaClass.getResourceAsStream(fxmlFile)) as Parent
//      stage!!.title = "Conversim Client"
//      stage!!.scene = Scene(root)
//      stage!!.show()
//      val controller = loader.getController<Any>()
//      controller.setParams(stage, txtUsername!!.text, txtPassword!!.text, (answer.getObject() as UserJSON).id)
    } catch (ex: Exception) {
      System.err.println(ex.toString())
    }

  }

}