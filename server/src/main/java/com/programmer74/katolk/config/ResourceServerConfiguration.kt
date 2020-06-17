package com.programmer74.katolk.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer

@Configuration
@EnableResourceServer
class ResourceServerConfiguration : ResourceServerConfigurerAdapter() {

  override fun configure(resources: ResourceServerSecurityConfigurer) {
    resources.resourceId(RESOURCE_ID)
  }

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http
        .authorizeRequests()
        .antMatchers(SECURED_PATTERN).authenticated()
  }

  companion object {
    private const val RESOURCE_ID = "resource-server-rest-api"
    private const val SECURED_PATTERN = "/api/**"
  }
}
