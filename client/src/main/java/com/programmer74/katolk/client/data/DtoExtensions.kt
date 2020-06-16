package com.programmer74.katolk.client.data

import com.programmer74.katolk.dto.DialogueDto
import com.programmer74.katolk.dto.UserInfoDto

fun DialogueDto.getOpponent(me: UserInfoDto): UserInfoDto? {
  if (participants.count() > 2) {
    return null
  }
  if (participants.count() == 1) {
    return participants.first()
  }
  return participants.first { it.id != me.id }
}