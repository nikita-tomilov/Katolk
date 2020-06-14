package com.programmer74.katolk

import mu.KLogging
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class KatolkServerApplication {
  companion object : KLogging() {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(KatolkServerApplication::class.java, *args)
    }
  }
}
