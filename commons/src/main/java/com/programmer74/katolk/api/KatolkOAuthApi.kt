package com.programmer74.katolk.api

import com.programmer74.katolk.dto.OAuthResponseDto
import feign.RequestLine
import org.omg.CORBA.Object

interface KatolkOAuthApi {
  @RequestLine("POST /oauth/token")
  fun getToken(request: String): OAuthResponseDto
}