package com.programmer74.katolk.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

  @Throws(Exception::class)
  override fun configure(web: WebSecurity) {
    web.ignoring()
        .antMatchers(
            "/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/**",
            "/swagger-ui.html",
            "/webjars/**");
  }
}