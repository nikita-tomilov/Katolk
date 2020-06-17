package com.programmer74.katolk.dto

data class OAuthResponseDto(
  val access_token: String,
  val token_type: String,
  val refresh_token: String,
  val expires_in: Long,
  val scope: String
)