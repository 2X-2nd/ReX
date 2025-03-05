package com.example.hellofigma.message.repository

import android.content.Context
import androidx.room.Room
import com.example.hellofigma.message.model.Chat
import com.example.hellofigma.message.model.Message
import com.example.hellofigma.message.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant


class ChatRepository private constructor(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        ChatDatabase::class.java, "chat-db"
    ).build()

    // Repository手动实现单例模式,否则用Hilt依赖注入框架来实现单例,不单例会导致数据不能同步更新以及资源浪费
    companion object {
        @Volatile
        private var instance: ChatRepository? = null

        fun getInstance(context: Context): ChatRepository {
            return instance ?: synchronized(this) {
                instance ?: ChatRepository(context).also { instance = it }
            }
        }
    }

    val apiService = RetrofitClient.instance.create(ChatApi::class.java)

    // 获取聊天列表
    fun getChats(userId: String): Flow<List<Chat>> {
        return db.chatDao().getChats(userId)
    }

    // 添加新聊天
    suspend fun addChat(
        userId: String,
        otherUserId: String,
        otherUserName: String
    ): Flow<NetworkResult<Chat>> = flow { // 返回封装结果
        emit(NetworkResult.Loading)
        try {
            val dbChat = db.chatDao().getChat(userId, otherUserId)
            if (dbChat != null) {
                emit(NetworkResult.Success(dbChat))
            } else {
                val response = apiService.startChat(
                    StartChatRequest(
                        buyerId = userId,
                        sellerId = otherUserId
                    )
                )

                if (response.isSuccessful) {
                    val chat = Chat(
                        chatId = response.body()!!.chatId,
                        userId = userId,
                        otherUserId = otherUserId,
                        otherUserName = otherUserName,
                        lastMessage = "",
                        timestamp = System.currentTimeMillis()
                    )
                    db.chatDao().insertChat(chat)
                    emit(NetworkResult.Success(chat))
                } else {
                    emit(NetworkResult.Error("Failed to create chat: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.toString()))
        }
    }

    // 获取并保存聊天历史
    suspend fun getChatHistory(chatId: String, otherUserId: String, currentUserId: String): NetworkResult<String> {
        return try {
            val response = apiService.getChatHistory(chatId)
            if (response.isSuccessful) {
                response.body()?.let { history ->
                    // 转换远程消息为本地消息格式
                    val localMessages = history.messages.map { remoteMessage ->
                        Message(
                            messageId = remoteMessage.id.toString(),
                            chatId = otherUserId,
                            content = remoteMessage.message,
                            isSentByMe = remoteMessage.sender_id == currentUserId,
                            timestamp = Instant.parse(remoteMessage.timestamp).toEpochMilli()
                        )
                    }
                    // 批量保存到数据库
                    db.messageDao().insertMessages(localMessages)

                    updateLastMessage(currentUserId, otherUserId, localMessages.get(localMessages.size - 1).content, localMessages.get(localMessages.size - 1).timestamp)
                }
                NetworkResult.Success("Success")
            }
            else {
                NetworkResult.Error("Failed to create chat: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network connection failed: ${e.localizedMessage}")
        }
    }

    // 获取本地存储的聊天消息
    fun getLocalMessages(chatId: String): Flow<List<Message>> {
        return db.messageDao().getMessages(chatId)
    }

    suspend fun insertMessage(message: Message) {
        db.messageDao().insertMessage(message)
    }

    suspend fun updateMessageId(tempId: String, serverId: String) {
        db.messageDao().updateMessageId(tempId, serverId)
    }

    suspend fun updateLastMessage(userId:String, otherUserId: String, message: String, timestamp: Long) {
        db.chatDao().updateLastMessage(userId, otherUserId, message, timestamp)
    }
}

sealed class NetworkResult<out T>(
    val data: T? = null,
    val error: String? = null
) {
    class Success<out T>(data: T) : NetworkResult<T>(data)
    class Error<out T>(error: String, data: T? = null) : NetworkResult<T>(data, error)
    object Loading : NetworkResult<Nothing>()
}