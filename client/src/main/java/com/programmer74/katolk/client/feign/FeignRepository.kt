package com.programmer74.katolk.client.feign

import feign.Feign
import feign.auth.BasicAuthRequestInterceptor
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import feign.okhttp.OkHttpClient
import java.lang.Exception

class FeignRepository(
  private val url: String,
  private val username: String,
  private val password: String
) {

  private val token: String by lazy {
    obtainToken()
  }

  fun getUserClient(): UserClient = Feign.builder()
      .client(OkHttpClient())
      .encoder(GsonEncoder())
      .decoder(GsonDecoder())
      .requestInterceptor {
        it.header("Authorization", "Bearer $token")
      }
      .target(UserClient::class.java, "$url/api/user")

  fun getDialogueClient(): DialogueClient = Feign.builder()
      .client(OkHttpClient())
      .encoder(GsonEncoder())
      .decoder(GsonDecoder())
      .requestInterceptor {
        it.header("Authorization", "Bearer $token")
        it.header("Content-Type", "application/json")
      }
      .target(DialogueClient::class.java, "$url/api/dialog")

  fun getWsClient() = WsClient(username, password, "$url/api/ws/websocket", token)

  private fun obtainToken(): String {
    val oAuth2Client = Feign.builder()
        .client(OkHttpClient())
        .decoder(GsonDecoder())
        .requestInterceptor {
          val bs = BasicAuthRequestInterceptor("oauth2-client", "oauth2-client-password")
          bs.apply(it)
          it.header("Content-Type", "application/x-www-form-urlencoded")
        }
        .target(OAuth2Client::class.java, url)
    try {
      val response =
          oAuth2Client.getToken("grant_type=password&username=$username&password=$password")
      println(response)
      return response.access_token
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    }
  }
}