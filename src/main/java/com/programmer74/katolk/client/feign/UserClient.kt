package com.programmer74.katolk.client.feign

import com.programmer74.katolk.common.data.User
import feign.Param
import feign.RequestLine

interface UserClient {
  @RequestLine("GET /me")
  fun me(): User

  @RequestLine("GET /user/{id}")
  fun getUser(@Param("id") id: Int): User
}