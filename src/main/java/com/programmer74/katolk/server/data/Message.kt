package com.programmer74.katolk.server.data

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Message(
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  val id: Int,
  val author: Int,
  val dialogID: Int,
  val body: String,
  var date: Long
)