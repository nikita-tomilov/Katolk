package com.programmer74.katolk.client.feign

import com.programmer74.katolk.common.data.User
import feign.RequestLine

interface UserClient {
  @RequestLine("GET /me")
  fun me(): User
}