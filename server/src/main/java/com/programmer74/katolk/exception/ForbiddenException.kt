package com.programmer74.katolk.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException : RuntimeException()