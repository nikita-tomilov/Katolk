package com.programmer74.katolk.client.gui

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.programmer74.katolk.FeignRepository
import com.programmer74.katolk.client.data.Preferences
import com.programmer74.katolk.dto.UserInfoDto
import feign.FeignException
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.io.File
import java.io.File.separator
import java.nio.file.Files

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

    val me: UserInfoDto
    try {
      feignRepo.obtainToken()
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
      stage!!.scene.stylesheets.add("fxml/style.css")
      stage!!.show()
      stage!!.setOnCloseRequest {
        System.exit(0)
      }
      val controller = loader.getController<Any>() as MainController
      controller.feignRepository = feignRepo
      controller.performPostConstruct()
      performPreDestroy()
    } catch (ex: Exception) {
      System.err.println(ex.toString())
    }
  }

  fun performPostConstruct() {
    try {
      val settingsPath = buildSettingsPath()
      val json = Files.readAllBytes(File(settingsPath).toPath())
      val obj = ObjectMapper().registerKotlinModule().readValue(json, Preferences::class.java)
      txtUrl.text = obj.ip
      txtUsername.text = obj.username
      txtPassword.text = obj.password
    } catch (e: Exception) {
      println("Unable to read settings file")
      e.printStackTrace()
    }
  }

  private fun performPreDestroy() {
    try {
      val settingsPath = buildSettingsPath()
      val preferences = Preferences(txtUrl.text, txtUsername.text, txtPassword.text)
      val json = ObjectMapper().registerKotlinModule().writeValueAsString(preferences)
      Files.write(File(settingsPath).toPath(), json.toByteArray())
    } catch (e: Exception) {
      println("Unable to save settings file")
    }
  }

  private fun buildSettingsPath(): String {
    val path = System.getProperty("user.home") + separator + ".katolk"
    val customDir = File(path)
    if (!(customDir.exists() || customDir.mkdirs())) {
      System.err.println("Path $path could not be created or accessed")
    }
    return path + separator + "settings.json"
  }
}