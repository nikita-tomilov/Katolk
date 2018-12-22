package com.programmer74.katolk.server.config

import com.programmer74.katolk.server.auth.CustomAuthProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@EnableWebSecurity
@Configuration
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

  @Autowired
  lateinit var customAuthProvider: CustomAuthProvider

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http.authorizeRequests().anyRequest().authenticated()
        .and()
        .csrf().disable()
        .httpBasic()
  }

  @Throws(Exception::class)
  override fun configure(auth: AuthenticationManagerBuilder) {
    auth.authenticationProvider(customAuthProvider)
  }
}
