package com.programmer74.katolk.dao

import javax.persistence.*

@Entity
@Table(name = "message")
data class MessageEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = 0,

  @Column(nullable = false)
  val author: Long = 0,

  @Column(name = "dialogue_id")
  val dialogueID: Long = 0,

  @Column(nullable = false)
  val body: String = "",

  @Column(nullable = false)
  var timestamp: Long = 0,

  @Column(name = "was_read", nullable = false)
  var wasRead: Boolean = false
) {
  fun safeId(): Long = id ?: error("should-never-happen")
}