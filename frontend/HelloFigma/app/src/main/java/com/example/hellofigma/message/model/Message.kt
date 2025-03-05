package com.example.hellofigma.message.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Message(
    @PrimaryKey val messageId: String,
    val chatId: String,
    val content: String,
    val isSentByMe: Boolean,
    val timestamp: Long
)
