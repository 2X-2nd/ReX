package com.example.hellofigma.message

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hellofigma.message.model.Chat
import com.example.hellofigma.message.model.Message
import com.example.hellofigma.message.repository.ChatRepository
import com.example.hellofigma.message.repository.NetworkResult
import com.example.hellofigma.message.repository.SendMessageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _loadChatHistoryState = MutableStateFlow<NetworkResult<Unit>?>(null)
    val loadChatHistoryState: StateFlow<NetworkResult<Unit>?> = _loadChatHistoryState.asStateFlow()
    fun resetLoadChatHistoryState() {
        _loadChatHistoryState.value = null
    }

    private val repository = ChatRepository.getInstance(application)

    suspend fun getChatList(userId: String) {
        repository.getChatList(userId);
    }

    private val _chats = MutableStateFlow<List<Chat>?>(null)
    val chats: StateFlow<List<Chat>?> = _chats.asStateFlow()
    fun loadChats(userId: String) {
        viewModelScope.launch {
            repository.getChats(userId).collect { chatList ->
                _chats.value = chatList
            }
        }
    }

    private val _addChatState = mutableStateOf<NetworkResult<Chat>>(NetworkResult.Loading)
    val addChatState: State<NetworkResult<Chat>> = _addChatState
    fun addChat(userId: String, otherUserId: String, otherUserName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addChat(userId, otherUserId, otherUserName)
                .collect { result ->
                    _addChatState.value = result
                }
        }
    }

    fun loadChatHistory(chatId: String, userId: String, otherUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.getChatHistory(chatId = chatId, otherUserId = otherUserId, currentUserId = userId)
                withContext(Dispatchers.Main) {
                    _loadChatHistoryState.value = when (result) {
                        is NetworkResult.Success -> {
                            NetworkResult.Success(Unit)
                        }
                        is NetworkResult.Error -> {
                            NetworkResult.Error(result.error ?: "Load chat history fail")
                        }
                        else -> NetworkResult.Error("Unknown error")
                    }
                }
            } catch (e: Exception) {
                _loadChatHistoryState.value = NetworkResult.Error("Add fail: ${e.message}")
            }
        }
    }

    fun getMessages(chatId: String): Flow<List<Message>> {
        return repository.getLocalMessages(chatId)
    }

    fun sendMessage(chatId: String, userId: String, otherUserId: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 调用API发送消息
                repository.apiService.sendMessage(
                    chatId = chatId,
                    request = SendMessageRequest(
                        senderId = userId,
                        message = message
                    )
                )

                repository.getChatHistory(chatId, otherUserId, userId)
            } catch (e: Exception) {
                // 处理发送失败情况
                e.printStackTrace()
            }
        }
    }
}
