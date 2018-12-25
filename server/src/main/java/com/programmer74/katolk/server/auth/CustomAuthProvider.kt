package com.programmer74.katolk.server.auth

import com.programmer74.katolk.server.repositories.UserVault
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class CustomAuthProvider @Autowired
constructor(private val userVault: UserVault) : AuthenticationProvider {

  @Throws(AuthenticationException::class)
  override fun authenticate(authentication: Authentication): Authentication {

    val username = authentication.name
    val password = authentication.credentials.toString()

    val user = userVault.repository.findByUsername(username) ?: throw BadCredentialsException("Bad credentials")
    if (!userVault.checkPasswordMatches(user, password)) {
      throw BadCredentialsException("Bad credentials")
    }
    val authorities = setOf(SimpleGrantedAuthority("ROLE_USER"))
    return UsernamePasswordAuthenticationToken(username, password, authorities)
  }

  override fun supports(authentication: Class<*>): Boolean {
    return authentication == UsernamePasswordAuthenticationToken::class.java
  }
}
