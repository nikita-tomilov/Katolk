package com.programmer74.katolk

import com.programmer74.katolk.api.DialogueAPI
import com.programmer74.katolk.api.KatolkOAuthApi
import com.programmer74.katolk.api.UserAPI
import com.programmer74.katolk.dto.OAuthResponseDto
import feign.Feign
import feign.auth.BasicAuthRequestInterceptor
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import feign.okhttp.OkHttpClient
import java.io.Serializable
import java.util.concurrent.atomic.AtomicReference

class FeignRepository(
  val url: String
) : Serializable {

  private val tokenRefDto = AtomicReference<OAuthResponseDto>()

  fun getAccessToken(): String = tokenRefDto.get().access_token

  fun getRefreshToken(): String = tokenRefDto.get().refresh_token

  fun getToken(): OAuthResponseDto = tokenRefDto.get()

  fun getUserClient(): UserAPI = Feign.builder()
      .client(OkHttpClient())
      .encoder(GsonEncoder())
      .decoder(GsonDecoder())
      .requestInterceptor {
        it.header("Authorization", "Bearer ${getAccessToken()}")
      }
      .target(UserAPI::class.java, "$url/api/user")

  fun getDialogueClient(): DialogueAPI = Feign.builder()
      .client(OkHttpClient())
      .encoder(GsonEncoder())
      .decoder(GsonDecoder())
      .requestInterceptor {
        it.header("Authorization", "Bearer ${getAccessToken()}")
        it.header("Content-Type", "application/json")
      }
      .target(DialogueAPI::class.java, "$url/api/dialog")

  fun isTokenObtained() = (tokenRefDto.get() != null)

  fun obtainTokenByUsernamePassword(username: String, password: String): String {
    val oAuth2Client = buildOAuthClient()
    val response =
        oAuth2Client.getToken("grant_type=password&username=$username&password=$password")
    val token = response.access_token
    tokenRefDto.set(response)
    return token
  }

  fun obtainTokenByRefreshToken(refreshToken: String): String {
    val oAuth2Client = buildOAuthClient()
    val response =
        oAuth2Client.getToken("grant_type=refresh_token&refresh_token=$refreshToken")
    val token = response.access_token
    tokenRefDto.set(response)
    return token
  }

  private fun buildOAuthClient() = Feign.builder()
      .client(OkHttpClient())
      .decoder(GsonDecoder())
      .requestInterceptor {
        val bs = BasicAuthRequestInterceptor("oauth2-client", "oauth2-client-password")
        bs.apply(it)
        it.header("Content-Type", "application/x-www-form-urlencoded")
      }
      .target(KatolkOAuthApi::class.java, url)
}