package com.programmer74.katolk.client.feign

import com.programmer74.katolk.client.data.UserJson
import feign.Param
import feign.RequestLine

interface UserClient {
  @RequestLine("GET /me")
  fun me(): UserJson

  @RequestLine("GET /user/{id}")
  fun getUser(@Param("id") id: Int): UserJson
}