package com.programmer74.katolk.client.gui

import com.programmer74.katolk.client.feign.FeignRepository
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

class LoginFrame: JFrame("Login") {
  init {
    size = Dimension(300, 300)
    preferredSize = size
    minimumSize = size
    maximumSize = size
    setLocationRelativeTo(null)
    addWindowListener(object : WindowAdapter() {
      override fun windowClosing(e: WindowEvent?) {
        System.exit(0)
      }
    })

    val panel = JPanel()
    panel.layout = FlowLayout()

    val button = JButton("test")
    button.addActionListener {
      val feignRepo = FeignRepository("http://localhost:8080", "admin", "admin")
      val userClient = feignRepo.getUserClient()
      println(userClient.me())
    }

    panel.add(button)
    add(panel)
  }
}