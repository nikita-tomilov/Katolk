package com.programmer74.katolk.api

import com.programmer74.katolk.dto.UserInfoDto
import feign.Param
import feign.RequestLine

interface UserAPI {
  @RequestLine("GET /me")
  fun me(): UserInfoDto

  @RequestLine("GET /user/{id}")
  fun getUser(@Param("id") id: Long): UserInfoDto

  @RequestLine("GET /me/change/password/{newpassword}")
  fun changeMyPassword(@Param("newpassword") newPassword: String): UserInfoDto

  @RequestLine("GET /user/all")
  fun getAllUsers(): List<UserInfoDto>
}