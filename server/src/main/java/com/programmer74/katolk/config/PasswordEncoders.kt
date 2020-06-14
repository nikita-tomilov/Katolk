package com.programmer74.katolk.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordEncoders {
  @Bean
  fun oauthClientPasswordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder(4)
  }

  @Bean
  fun userPasswordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder(8)
  }
}