package com.programmer74.katolk.dao

import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.dto.UserInfoDto
import com.programmer74.katolk.repository.OnlineUserRepositorySingleton

fun userInfoDtoFromUser(user: User): UserInfoDto {
  return UserInfoDto(
      user.id!!,
      user.username,
      OnlineUserRepositorySingleton.get().isUserOnline(user),
      "name ${user.id}",
      "surname ${user.id}",
      0L,
      '?',
      "city",
      user.authorities.map { it.authority },
      0L)
}

fun MessageEntity.toDto(authorName: String) = MessageDto(id!!, authorName, author, dialogueID, body)