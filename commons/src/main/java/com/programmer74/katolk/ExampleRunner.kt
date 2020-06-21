package com.programmer74.katolk

import mu.KLogging
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

object ExampleRunner : KLogging() {

  @JvmStatic
  fun main(args: Array<String>) {
    val fr = FeignRepository("http://localhost:8080")
    val token = fr.obtainTokenByUsernamePassword("user1", "user1password")
    logger.warn { "token: $token" }

    val katolkModel = KatolkModel(fr)
    katolkModel.setup(Consumer { logger.warn { "user info: $it" } })

    CountDownLatch(1).await()
  }
}