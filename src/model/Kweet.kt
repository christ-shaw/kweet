package com.xzb.model

import java.io.*
import java.time.LocalDateTime

data class Kweet(val id: Int, val userId: String, val text: String, val date: LocalDateTime, val replyTo: Int?) : Serializable
