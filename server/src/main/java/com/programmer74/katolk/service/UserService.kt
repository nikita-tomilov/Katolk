package com.programmer74.katolk.service

import com.programmer74.katolk.dao.Authority
import com.programmer74.katolk.dao.User
import com.programmer74.katolk.dto.UserDto
import com.programmer74.katolk.exception.NotFoundException
import com.programmer74.katolk.repository.UserRepository
import com.programmer74.katolk.util.SecurityContextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
  private val userRepository: UserRepository,
  private val userPasswordEncoder: PasswordEncoder
) : UserDetailsService {

  @Autowired
  lateinit var dialogueService: DialogueService

  @Transactional(readOnly = true)
  @Throws(UsernameNotFoundException::class)
  override fun loadUserByUsername(username: String): UserDetails {
    val user = loadUserByUsernameOrNull(username)
    if (user != null) {
      return user
    }
    throw UsernameNotFoundException(username)
  }

  fun loadUserByUsernameOrNull(username: String): User? {
    return userRepository.findByUsername(username)
  }

  fun findUserById(id: Long): User {
    return userRepository.findByIdOrNull(id) ?: throw NotFoundException("no user with id $id found")
  }

  fun findUsersByIds(ids: List<Long>): List<User> {
    //TODO: optimize
    return ids.map { findUserById(it) }
  }

  fun me(): UserDto {
    val user = SecurityContextUtils.getUserFromContext()
    return UserDto(user.username, user.authorities.map { it.authority })
  }

  fun all(): List<User> {
    return userRepository.findAll()
  }

  fun meAsEntity(): User {
    val user = SecurityContextUtils.getUserFromContext()
    return userRepository.findByUsername(user.username)!!
  }

  fun createUser(username: String, password: String): User {
    val latestId = userRepository.findAll().map { it.id ?: 0L }.max() ?: 0L //TODO: fix
    val newUser = User(
        id = latestId + 1,
        username = username,
        password = userPasswordEncoder.encode(password),
        accountExpired = false,
        accountLocked = false,
        credentialsExpired = false,
        enabled = true,
        authorities = listOf(Authority(2, "USER"))
    )
    val savedUser = userRepository.saveAndFlush(newUser)
    dialogueService.onboardUser(savedUser)
    return savedUser
  }

  fun changePassword(newPassword: String): User {
    val user = SecurityContextUtils.getUserFromContext()
    val userFromDatabase = loadUserByUsernameOrNull(user.username) ?: error("should-never-happen")
    userFromDatabase.setNewPassword(userPasswordEncoder.encode(newPassword))
    return userRepository.saveAndFlush(userFromDatabase)
  }
}