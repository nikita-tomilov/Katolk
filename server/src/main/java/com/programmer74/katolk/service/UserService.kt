package com.programmer74.katolk.service

import com.programmer74.katolk.dao.User
import com.programmer74.katolk.dto.SmallUserDto
import com.programmer74.katolk.exception.NotFoundException
import com.programmer74.katolk.repository.UserRepository
import com.programmer74.katolk.util.SecurityContextUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
  private val userRepository: UserRepository
) : UserDetailsService {

  @Transactional(readOnly = true)
  @Throws(UsernameNotFoundException::class)
  override fun loadUserByUsername(username: String): UserDetails {
    val user = loadUserByUsernameOrNull(username)
    if (user != null) {
      return user
    }
    throw UsernameNotFoundException(username)
  }

  private fun loadUserByUsernameOrNull(username: String): User? {
    return userRepository.findByUsername(username)
  }

  fun findUserById(id: Long): User {
    return userRepository.findByIdOrNull(id) ?: throw NotFoundException("no user with id $id found")
  }

  fun findUsersByIds(ids: List<Long>): List<User> {
    //TODO: optimize
    return ids.map { findUserById(it) }
  }

  fun me(): SmallUserDto {
    val user = SecurityContextUtils.getUserFromContext()
    return SmallUserDto(user.username, user.authorities.map { it.authority })
  }

  fun meAsEntity(): User {
    val user = SecurityContextUtils.getUserFromContext()
    return userRepository.findByUsername(user.username)!!
  }

  fun myId(): Long {
    return meAsEntity().id!!
  }

  fun IAmAdmin(): Boolean {
    return me().roles.filter { it.contains("ADMIN") }.isNotEmpty()
  }
}