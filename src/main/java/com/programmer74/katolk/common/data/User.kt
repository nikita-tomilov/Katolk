package com.programmer74.katolk.common.data

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class User(
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  val id: Int = 0,
  val username: String = "",
  var password: String = ""
)