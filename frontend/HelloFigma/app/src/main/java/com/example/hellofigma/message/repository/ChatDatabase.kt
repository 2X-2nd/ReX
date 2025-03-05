package com.example.hellofigma.message.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.hellofigma.message.model.Chat
import com.example.hellofigma.message.model.Message


@Database(entities = [Chat::class, Message::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
