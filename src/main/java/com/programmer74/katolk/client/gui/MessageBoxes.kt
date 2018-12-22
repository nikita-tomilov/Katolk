package com.programmer74.katolk.client.gui

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

object MessageBoxes {

  private var alertResult = false
  private var alertFired = false

  //alerts
  fun showAlert(prompt: String, title: String) {
    Platform.runLater {
      val msgbox = Alert(Alert.AlertType.INFORMATION)
      msgbox.title = title
      msgbox.contentText = prompt
      msgbox.showAndWait()
    }
  }

  fun showCriticalErrorAlert(prompt: String, title: String) {
    Platform.runLater {
      val msgbox = Alert(Alert.AlertType.ERROR)
      msgbox.title = title
      msgbox.contentText = prompt
      msgbox.showAndWait()
      System.exit(-1)
    }
  }

  private fun showYesNoAlertFinished(result: Boolean) {
    alertResult = result
    alertFired = true
  }

  fun showYesNoAlert(prompt: String): Boolean {
    alertResult = false
    alertFired = false
    Platform.runLater {
      val alert = Alert(Alert.AlertType.CONFIRMATION, prompt, ButtonType.YES, ButtonType.NO)
      alert.showAndWait()
      showYesNoAlertFinished(alert.result == ButtonType.YES)
    }
    while (!alertFired) {
      Thread.yield()
    }
    return alertResult
  }
}