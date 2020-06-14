package com.programmer74.katolk.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
@Import(com.programmer74.katolk.config.PasswordEncoders::class)
@DependsOn(value = ["flyway", "flywayInitializer"])
@Order(SecurityProperties.BASIC_AUTH_ORDER + 1)
class ServerSecurityConfig(
  @Autowired private val userDetailsService: UserDetailsService,
  @Autowired private val userPasswordEncoder: PasswordEncoder
) : WebSecurityConfigurerAdapter() {

  @Bean
  @Throws(Exception::class)
  override fun authenticationManagerBean(): AuthenticationManager {
    return super.authenticationManagerBean()
  }

  @Throws(Exception::class)
  override fun configure(auth: AuthenticationManagerBuilder?) {
    auth!!.userDetailsService(userDetailsService)
        .passwordEncoder(userPasswordEncoder)
  }
}