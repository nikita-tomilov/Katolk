package com.programmer74.katolk.server.controllers

import com.programmer74.katolk.server.entity.UserEntity
import com.programmer74.katolk.server.entity.UserJson
import com.programmer74.katolk.server.repositories.UserVault
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/user"])
class UserController {

  @Autowired
  lateinit var users: UserVault

  @GetMapping("/me")
  fun me(): UserJson {
   return UserJson.from(users.getCurrentUser())
  }

  @GetMapping("/user/{id}")
  fun me(
      @PathVariable("id") id: Int
  ): UserJson {
    return UserJson.from(users.getUser(id))
  }
}