package com.programmer74.katolk.client.feign

import feign.RequestLine
import org.omg.CORBA.Object

interface OAuth2Client {
  @RequestLine("POST /oauth/token")
  fun getToken(request: String): OAuth2Response
}