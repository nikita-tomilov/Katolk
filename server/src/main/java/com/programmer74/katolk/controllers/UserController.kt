package com.programmer74.katolk.controllers

import com.programmer74.katolk.dto.UserDto
import com.programmer74.katolk.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/user"])
class UserController(
  private val userService: UserService
) {

  @GetMapping("/me")
  fun me(): UserDto {
    return UserDto.from(userService.meAsEntity())
  }

  @GetMapping("/user/{id}")
  fun me(
    @PathVariable("id") id: Long
  ): UserDto {
    return UserDto.from(userService.findUserById(id))
  }
}