package com.programmer74.katolk.client.feign

data class OAuth2Response(
  val access_token: String,
  val token_type: String,
  val refresh_token: String,
  val expires_in: Long,
  val scope: String
)