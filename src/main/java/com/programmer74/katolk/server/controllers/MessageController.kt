package com.programmer74.katolk.server.controllers

import com.programmer74.katolk.common.data.Message
import com.programmer74.katolk.server.repositories.MessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping(path = ["/api/message"])
class MessageController {

  @Autowired
  lateinit var messages: MessageRepository

  @GetMapping("/all")
  fun msg(): List<Message> {
    return messages.findAll().toList()
  }

  @PostMapping("/send")
  fun sendMsg(@RequestBody msg: Message): Message {
    msg.date = Instant.now().toEpochMilli()
    return messages.save(msg)
  }
}