package com.programmer74.katolk.controllers

import com.programmer74.katolk.api.UserAPI
import com.programmer74.katolk.dao.userInfoDtoFromUser
import com.programmer74.katolk.dto.UserInfoDto
import com.programmer74.katolk.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/user"])
class UserController(
  private val userService: UserService
) : UserAPI {

  @GetMapping("/me")
  override fun me(): UserInfoDto {
    return userInfoDtoFromUser(userService.meAsEntity())
  }

  @GetMapping("/user/{id}")
  override fun getUser(
    @PathVariable("id") id: Long
  ): UserInfoDto {
    return userInfoDtoFromUser(userService.findUserById(id))
  }
}