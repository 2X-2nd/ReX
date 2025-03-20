package com.example.hellofigma.message.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.hellofigma.message.model.Chat
import com.example.hellofigma.message.model.Message
import com.example.hellofigma.message.network.ChatUserRetrofitClient
import com.example.hellofigma.message.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import com.google.gson.JsonParseException
import java.time.Instant
import java.time.format.DateTimeParseException

class ChatRepository private constructor(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        ChatDatabase::class.java, "chat-db"
    ).build()

    // Repository 手动实现单例模式，或用 Hilt 依赖注入来实现单例
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
    val chatUserApiService = ChatUserRetrofitClient.instance.create(ChatUserApi::class.java)

    suspend fun getChatList(userId: String) {
        if (userId.isEmpty()) return

        try {
            val response = apiService.getChatList(userId)
            if (response.isSuccessful) {
                response.body()?.let { history ->
                    val chats = history.chatIds
                        .filter { chat ->
                            // 只处理数据库中不存在的记录
                            db.chatDao().getChatByChatId(userId, chat.id) == null
                        }
                        .map { chat ->
                            var otherUserId = ""
                            var lastMessage = ""
                            var lastTimestamp = "No timestamp"

                            // 获取聊天历史，提取最后一条消息信息
                            val response2 = apiService.getChatHistory(chat.id)
                            if (response2.isSuccessful) {
                                response2.body()?.let { history2 ->
                                    val users = history2.messages
                                        .filter { it.sender_id != userId }
                                        .map { it.sender_id }
                                        .distinct()
                                    otherUserId = users.firstOrNull() ?: ""
                                    val lastMsg = history2.messages.lastOrNull()
                                    lastMessage = lastMsg?.message ?: "No messages"
                                    lastTimestamp = lastMsg?.timestamp ?: "No timestamp"
                                }
                            }

                            var otherUserName = otherUserId
                            if (otherUserId.isNotEmpty()) {
                                try {
                                    val result = chatUserApiService.getUser(otherUserId)
                                    otherUserName = result.username
                                } catch (e: IOException) { // 网络错误
                                    Log.e("ChatRepository", "Network error while fetching user info: ${e.message}", e)
                                } catch (e: HttpException) { // HTTP 错误
                                    Log.e("ChatRepository", "HTTP error ${e.code()} while fetching user info", e)
                                } catch (e: JsonParseException) { // JSON 解析错误
                                    Log.e("ChatRepository", "JSON parsing error while fetching user info", e)
                                }
                            }
                            Chat(
                                chatId = chat.id.toString(),
                                userId = userId,
                                otherUserId = chat.id.toString(),
                                otherUserName = otherUserName,
                                lastMessage = lastMessage,
                                timestamp = parseTimestamp(lastTimestamp)
                            )
                        }
                    // 批量保存到数据库
                    db.chatDao().insertChats(chats)
                }
                // 这里可以返回或通知成功，根据需要扩展逻辑
                NetworkResult.Success("Success")
            } else {
                NetworkResult.Error("Failed to create chat: ${response.code()}")
            }
        } catch (e: IOException) {
            Log.e("ChatRepository", "Network error while fetching chat list: ${e.message}", e)
        } catch (e: HttpException) {
            Log.e("ChatRepository", "HTTP error ${e.code()} while fetching chat list", e)
        } catch (e: JsonParseException) {
            Log.e("ChatRepository", "JSON parsing error while fetching chat list: ${e.message}", e)
        }
    }

    private fun parseTimestamp(isoTime: String?): Long {
        if (isoTime == null) {
            Log.e("TimestampParser", "Timestamp is null")
            return 0L
        }
        return try {
            Instant.parse(isoTime).toEpochMilli()
        } catch (e: DateTimeParseException) {
            Log.e("TimestampParser", "Invalid date format: $isoTime", e)
            0L
        }
    }

    // 获取本地存储的聊天列表
    fun getChats(userId: String): Flow<List<Chat>> = db.chatDao().getChats(userId)

    // 添加新聊天
    suspend fun addChat(
        userId: String,
        otherUserId: String,
        otherUserName: String
    ): Flow<NetworkResult<Chat>> = flow {
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
        } catch (e: IOException) {
            Log.e("FetchData", "Network error: ${e.message}", e)
            emit(NetworkResult.Error("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Log.e("FetchData", "HTTP error ${e.code()}: ${e.message}", e)
            emit(NetworkResult.Error("Server error: ${e.code()}"))
        } catch (e: JsonParseException) {
            Log.e("FetchData", "JSON parsing error: ${e.message}", e)
            emit(NetworkResult.Error("Data parsing error"))
        } catch (e: IllegalStateException) {
            Log.e("FetchData", "Illegal state: ${e.message}", e)
            emit(NetworkResult.Error("Illegal state: ${e.message}"))
        }
    }

    // 获取并保存聊天历史
    suspend fun getChatHistory(chatId: String, otherUserId: String, currentUserId: String): NetworkResult<String> {
        return try {
            val response = apiService.getChatHistory(chatId)
            val history = response.body() ?: return NetworkResult.Error("No chat history found")
            val localMessages = history.messages.mapNotNull { remoteMessage ->
                try {
                    Message(
                        messageId = remoteMessage.id.toString(),
                        chatId = otherUserId,
                        content = remoteMessage.message ?: "No content",
                        isSentByMe = remoteMessage.sender_id == currentUserId,
                        timestamp = remoteMessage.timestamp?.let { Instant.parse(it).toEpochMilli() } ?: 0L
                    )
                } catch (e: DateTimeParseException) {
                    Log.e("getChatHistory", "Timestamp parsing error: ${remoteMessage.timestamp}", e)
                    null
                }
            }
            if (localMessages.isNotEmpty()) {
                db.messageDao().insertMessages(localMessages)
                localMessages.lastOrNull()?.let { lastMessage ->
                    updateLastMessage(currentUserId, otherUserId, lastMessage.content, lastMessage.timestamp)
                }
            }
            NetworkResult.Success("Success")
        } catch (e: IOException) {
            Log.e("getChatHistory", "Network error: ${e.message}", e)
            NetworkResult.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Log.e("getChatHistory", "HTTP error: ${e.code()} ${e.message}", e)
            NetworkResult.Error("HTTP error: ${e.code()}")
        } catch (e: JsonParseException) {
            Log.e("getChatHistory", "JSON parsing error", e)
            NetworkResult.Error("Data parsing error")
        }
    }

    // 获取本地存储的聊天消息
    fun getLocalMessages(chatId: String): Flow<List<Message>> = db.messageDao().getMessages(chatId)

    suspend fun insertMessage(message: Message) {
        db.messageDao().insertMessage(message)
    }

    suspend fun updateMessageId(tempId: String, serverId: String) {
        db.messageDao().updateMessageId(tempId, serverId)
    }

    suspend fun updateLastMessage(userId: String, otherUserId: String, message: String, timestamp: Long) {
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
