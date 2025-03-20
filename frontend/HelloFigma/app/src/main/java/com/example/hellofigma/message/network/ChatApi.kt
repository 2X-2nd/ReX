package com.example.hellofigma.message.repository

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


// Retrofit接口
interface ChatApi {
    @GET("chat/user/{userId}")
    suspend fun getChatList(@Path("userId") userId: String): Response<ChatListResponse>

    @POST("chat/start")
    suspend fun startChat(@Body request: StartChatRequest): Response<StartChatResponse>

    @GET("chat/{chatId}")
    suspend fun getChatHistory(@Path("chatId") chatId: String): Response<ChatHistoryResponse>

    @POST("chat/{chatId}/message")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body request: SendMessageRequest
    ): Response<MessageResponse>
}

// 请求模型
data class StartChatRequest(
    val buyerId: String,
    val sellerId: String
)

// 响应模型
data class StartChatResponse(
    val chatId: String
)

data class SendMessageRequest(
    val senderId: String,
    val message: String
)

data class MessageResponse(
    val messageId: String,
    val content: String,
    val timestamp: Long
)

data class ChatHistoryResponse(
    val messages: List<RemoteMessage> // 接口返回的消息列表
)

// 远程消息模型
data class RemoteMessage(
    val id: Long,
    val chat_id: Long,
    val sender_id: String,
    val message: String,
    val timestamp: String
)

data class ChatListResponse(
    val chatIds: List<ChatListId>
)

data class ChatListId(
    val id: String
)
