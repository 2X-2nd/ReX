package com.example.hellofigma.message.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Chat(
    @PrimaryKey val chatId: String,
    val userId: String, // Current login user ID
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String,
    val timestamp: Long
)
