package com.programmer74.katolk.dao

import javax.persistence.*

@Entity
@Table(name = "dialogue")
data class DialogueEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = 0,

  @Column(name = "creator_id", nullable = false)
  val creatorId: Long = 0,

  @Column(nullable = false)
  val name: String = ""
) {
  fun safeId(): Long = id ?: error("should-never-happen")
}