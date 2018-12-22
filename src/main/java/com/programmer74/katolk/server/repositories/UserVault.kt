package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.common.data.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class UserVault(val repository: UserRepository,
                val passwordEncoder: PasswordEncoder) {

  fun saveUser(user: User) {
    user.password = passwordEncoder.encode(user.password)
    repository.save(user)
  }

  fun checkPasswordMatches(user: User, password: String): Boolean {
    return passwordEncoder.matches(password, user.password)
  }

  fun getCurrentUser(): User {
    val context = SecurityContextHolder.getContext()
    val username = context.authentication.principal as String
    val user = repository.findByUsername(username)!!
    user.password = ""
    return user
  }

  fun getUser(id: Int): User {
    val user = repository.findById(id).get()
    user.password = ""
    return user
  }

  @PostConstruct
  fun initUsers() {
    saveUser(User(0, "admin", "admin"))
    saveUser(User(0, "user1", "user1"))
    saveUser(User(0, "user2", "user2"))
  }
}