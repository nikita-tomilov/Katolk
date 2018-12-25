package com.programmer74.katolk.server.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class DialogueEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  val id: Int = 0,
  val creator: Int = 0,
  val name: String = ""
)