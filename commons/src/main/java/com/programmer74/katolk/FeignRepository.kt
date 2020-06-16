package com.programmer74.katolk

import com.programmer74.katolk.api.DialogueAPI
import com.programmer74.katolk.api.KatolkOAuthApi
import com.programmer74.katolk.api.UserAPI
import feign.Feign
import feign.auth.BasicAuthRequestInterceptor
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import feign.okhttp.OkHttpClient
import java.io.Serializable
import java.util.concurrent.atomic.AtomicReference

class FeignRepository(
  val url: String,
  val username: String,
  val password: String
) : Serializable {

  private val tokenRef = AtomicReference("")

  fun getToken(): String = tokenRef.get()

  fun getUserClient(): UserAPI = Feign.builder()
      .client(OkHttpClient())
      .encoder(GsonEncoder())
      .decoder(GsonDecoder())
      .requestInterceptor {
        it.header("Authorization", "Bearer ${getToken()}")
      }
      .target(UserAPI::class.java, "$url/api/user")

  fun getDialogueClient(): DialogueAPI = Feign.builder()
      .client(OkHttpClient())
      .encoder(GsonEncoder())
      .decoder(GsonDecoder())
      .requestInterceptor {
        it.header("Authorization", "Bearer ${getToken()}")
        it.header("Content-Type", "application/json")
      }
      .target(DialogueAPI::class.java, "$url/api/dialog")

  fun isTokenObtained() = tokenRef.get().isNotEmpty()

  fun obtainToken(): String {
    val oAuth2Client = Feign.builder()
        .client(OkHttpClient())
        .decoder(GsonDecoder())
        .requestInterceptor {
          val bs = BasicAuthRequestInterceptor("oauth2-client", "oauth2-client-password")
          bs.apply(it)
          it.header("Content-Type", "application/x-www-form-urlencoded")
        }
        .target(KatolkOAuthApi::class.java, url)
    val response =
        oAuth2Client.getToken("grant_type=password&username=$username&password=$password")
    val token = response.access_token
    tokenRef.set(token)
    return token
  }
}