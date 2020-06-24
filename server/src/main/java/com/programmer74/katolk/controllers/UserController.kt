package com.programmer74.katolk.controllers

import com.programmer74.katolk.api.UserAPI
import com.programmer74.katolk.dao.userInfoDtoFromUser
import com.programmer74.katolk.dto.UserInfoDto
import com.programmer74.katolk.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
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

  @GetMapping("/me/change/password/{newpassword}")
  override fun changeMyPassword(
    @PathVariable("newpassword") newPassword: String
  ): UserInfoDto {
    return userInfoDtoFromUser(userService.changePassword(newPassword))
  }

  @GetMapping("/user/{id}")
  override fun getUser(
    @PathVariable("id") id: Long
  ): UserInfoDto {
    return userInfoDtoFromUser(userService.findUserById(id))
  }

  @GetMapping("/create/{username}/{password}")
  @PreAuthorize("hasAuthority('ADMIN')")
  fun createUser(
    @PathVariable("username") username: String,
    @PathVariable("password") password: String
  ): UserInfoDto {
    return userInfoDtoFromUser(userService.createUser(username, password))
  }

  @GetMapping("/user/all")
  override fun getAllUsers(): List<UserInfoDto> {
    return userService.all().map { userInfoDtoFromUser(it) }
  }
}