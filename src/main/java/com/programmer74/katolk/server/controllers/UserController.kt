package com.programmer74.katolk.server.controllers

import com.programmer74.katolk.common.data.User
import com.programmer74.katolk.server.repositories.UserVault
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/user"])
class UserController {

  @Autowired
  lateinit var users: UserVault

  @GetMapping("/me")
  fun me(): User {
   return users.getCurrentUser()
  }
}