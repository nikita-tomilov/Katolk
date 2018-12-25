package com.programmer74.katolk.server.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class DialogueParticipantEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  val id: Int = 0,
  val dialogueID: Int = 0,
  val userID: Int = 0
)