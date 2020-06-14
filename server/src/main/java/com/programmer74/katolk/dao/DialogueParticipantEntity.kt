package com.programmer74.katolk.dao

import javax.persistence.*

@Entity
@Table(name = "dialogue_participant")
data class DialogueParticipantEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = 0,

  @Column(name = "dialogue_id", nullable = false)
  val dialogueID: Long = 0,

  @Column(name = "user_id", nullable = false)
  val userID: Long = 0
) {
  fun safeId(): Long = id ?: error("should-never-happen")
}