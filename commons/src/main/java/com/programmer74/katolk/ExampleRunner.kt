package com.programmer74.katolk

import mu.KLogging
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import javax.sound.sampled.AudioSystem

object ExampleRunner : KLogging() {

  @JvmStatic
  fun main(args: Array<String>) {
    val fr = FeignRepository("http://localhost:8080")
    val token = fr.obtainTokenByUsernamePassword("user1", "user1password")
    logger.warn { "token: $token" }

    val katolkModel = KatolkModel(fr)
    katolkModel.setup(Consumer { logger.warn { "user info: $it" } })


    val mixerInfos = AudioSystem.getMixerInfo()
    mixerInfos.forEach { thisMixerInfo ->
      System.out.println(
          "Mixer: " + thisMixerInfo.getDescription() +
              " [" + thisMixerInfo.getName() + "]");
      val thisMixer = AudioSystem.getMixer(thisMixerInfo)

      val sourceLines = thisMixer.sourceLines
      sourceLines.forEach { println("hui $it") }

      val targetLines = thisMixer.targetLines
      targetLines.forEach { println("pizda $it") }
    }

    CountDownLatch(1).await()
  }
}