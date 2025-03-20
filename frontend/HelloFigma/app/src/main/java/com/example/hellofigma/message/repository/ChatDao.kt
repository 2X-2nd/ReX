package com.example.hellofigma.message.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hellofigma.message.model.Chat
import kotlinx.coroutines.flow.Flow


@Dao
interface ChatDao {
    @Query("SELECT * FROM Chat WHERE userId = :userId ORDER BY timestamp DESC")
    fun getChats(userId: String): Flow<List<Chat>>

    @Query("SELECT * FROM Chat WHERE userId = :userId AND chatId = :chatId LIMIT 1")
    suspend fun getChatByChatId(userId: String, chatId: String): Chat?

    @Query("SELECT * FROM Chat WHERE userId = :userId AND otherUserId = :otherUserId LIMIT 1")
    fun getChat(userId: String, otherUserId: String): Chat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(messages: List<Chat>)

    @Query("UPDATE Chat SET lastMessage = :message, timestamp = :timestamp WHERE userId = :userId AND otherUserId = :otherUserId")
    suspend fun updateLastMessage(userId: String, otherUserId: String, message: String, timestamp: Long)
}
