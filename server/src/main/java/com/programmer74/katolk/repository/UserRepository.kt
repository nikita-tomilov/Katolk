package com.programmer74.katolk.repository

import com.programmer74.katolk.dao.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

  @Query(
      "SELECT DISTINCT user FROM User user " +
          "INNER JOIN FETCH user.authorities AS authorities " +
          "WHERE user.username = :username")
  fun findByUsername(@Param("username") username: String): User?
}
