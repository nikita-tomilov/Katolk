package com.programmer74.katolk.server.repositories

import com.programmer74.katolk.server.data.Message
import org.springframework.data.repository.CrudRepository

public interface MessageRepository : CrudRepository<Message, Int> {

}