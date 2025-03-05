package com.example.hellofigma.message.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hellofigma.message.model.Message
import kotlinx.coroutines.flow.Flow


@Dao
interface MessageDao {
    @Query("SELECT * FROM Message WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessages(chatId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Query("UPDATE Message SET messageId = :serverId WHERE messageId = :tempId")
    suspend fun updateMessageId(tempId: String, serverId: String)
}
