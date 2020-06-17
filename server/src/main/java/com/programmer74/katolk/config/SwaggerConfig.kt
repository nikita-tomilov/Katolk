package com.programmer74.katolk.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {
  @Bean
  fun api(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build()
        .pathMapping("/")
        .securitySchemes(listOf(apiKey()))
        .securityContexts(listOf(securityContext()))
  }

  private fun apiKey(): ApiKey {
    return ApiKey("apiKey", "Authorization", "header")
  }

  private fun securityContext(): SecurityContext {
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .forPaths(PathSelectors.regex("/api.*"))
        .build()
  }

  private fun defaultAuth(): List<SecurityReference> {
    val authorizationScope = AuthorizationScope("global", "accessEverything")
    val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
    authorizationScopes[0] = authorizationScope
    return listOf(SecurityReference("apiKey", authorizationScopes))
  }
}