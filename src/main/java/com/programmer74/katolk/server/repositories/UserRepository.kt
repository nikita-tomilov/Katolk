package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.common.data.User
import org.springframework.data.repository.CrudRepository

public interface UserRepository : CrudRepository<User, Int> {
  fun findByUsername(username: String): User?
}