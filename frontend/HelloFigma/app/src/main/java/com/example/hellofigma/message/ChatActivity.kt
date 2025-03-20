package com.example.hellofigma.message

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.hellofigma.message.model.Message
import com.example.hellofigma.message.repository.NetworkResult
import com.example.hellofigma.ui.theme.HelloFigmaTheme
import kotlinx.coroutines.delay


class ChatActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("userId") ?: ""
        val otherUserId = intent.getStringExtra("otherUserId") ?: ""
        val otherUserName = intent.getStringExtra("otherUserName") ?: ""

        setContent {
            HelloFigmaTheme {
                // FullScreenScreen(this)
                StartChatScreen(
                    userId = userId,
                    otherUserId = otherUserId,
                    otherUserName = otherUserName,
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun StartChatScreen(
    userId: String,
    otherUserId: String,
    otherUserName: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.addChat(userId, otherUserId, otherUserName)
    }

    when (val state = viewModel.addChatState.value) {
        is NetworkResult.Loading -> {
        }
        is NetworkResult.Success -> {
            val chatId = state.data?.chatId
            if (chatId != null) {
                ChatScreen(
                    userId = userId,
                    chatId = chatId,
                    otherUserId = otherUserId,
                    otherUserName = otherUserName,
                    viewModel = viewModel,
                    onBack = onBack
                )
            }
        }
        is NetworkResult.Error -> {
        }
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    chatId: String,
    otherUserId: String,
    otherUserName: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.getMessages(otherUserId).collectAsState(emptyList())
    var newMessage by remember { mutableStateOf("") }

    val loadChatHistoryState by viewModel.loadChatHistoryState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loadChatHistoryState) {
        when (loadChatHistoryState) {
            is NetworkResult.Success -> {
                viewModel.resetLoadChatHistoryState()
            }
            is NetworkResult.Error -> {
                Toast.makeText(
                    context,
                    "Fail: ${(loadChatHistoryState as NetworkResult.Error).error}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetLoadChatHistoryState()
            }
            is NetworkResult.Loading -> {
            }
            null -> {}
        }
    }

    // 加载历史消息
    /*
    LaunchedEffect(otherUserId) {
        viewModel.loadChatHistory(chatId, userId, otherUserId)
    }*/
    // 轮询加载历史消息
    LaunchedEffect(otherUserId) {
        while (true) {
            viewModel.loadChatHistory(chatId, userId, otherUserId)
            delay(6000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat with $otherUserName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 消息列表
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        MessageBubble(
                            chatName = otherUserName,
                            message = message,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // 输入框
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier.weight(1f).testTag("MessageInput"),
                        placeholder = { Text("Input message...") },
                        singleLine = false,
                        maxLines = 3
                    )

                    IconButton(
                        modifier = Modifier.testTag("SendButton"),
                        onClick = {
                            if (newMessage.isNotBlank()) {
                                viewModel.sendMessage(chatId, userId, otherUserId, newMessage)
                                newMessage = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun MessageBubble(chatName: String, message: Message, modifier: Modifier = Modifier) {
    val isSentByMe = message.isSentByMe
    val boxAlignment = getBoxAlignment(isSentByMe)
    val alignment = getMessageAlignment(isSentByMe)
    val bubbleStyle = getBubbleStyle(isSentByMe)
    val senderName = if (isSentByMe) "Me" else chatName

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = boxAlignment
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = alignment
        ) {
            if (!isSentByMe) {
                SenderNameText(senderName)
            }

            MessageCard(message.content, bubbleStyle)

            TimestampRow(message.timestamp, isSentByMe)
        }
    }
}

// **提取：获取对齐方式**
fun getBoxAlignment(isSentByMe: Boolean) = if (isSentByMe) Alignment.TopEnd else Alignment.TopStart
fun getMessageAlignment(isSentByMe: Boolean) = if (isSentByMe) Alignment.End else Alignment.Start

// **提取：获取气泡样式**
data class BubbleStyle(val backgroundColor: Color, val textColor: Color, val shape: RoundedCornerShape)

fun getBubbleStyle(isSentByMe: Boolean): BubbleStyle {
    return BubbleStyle(
        backgroundColor = if (isSentByMe) Color(0xFF2196F3) else Color(0xFFE0E0E0),
        textColor = if (isSentByMe) Color.White else Color.Black,
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = if (isSentByMe) 12.dp else 4.dp,
            bottomEnd = if (isSentByMe) 4.dp else 12.dp
        )
    )
}

// **提取：显示发送者名称**
@Composable
fun SenderNameText(senderName: String) {
    Text(
        text = senderName,
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

// **提取：消息气泡**
@Composable
fun MessageCard(content: String, bubbleStyle: BubbleStyle) {
    Card(
        backgroundColor = bubbleStyle.backgroundColor,
        shape = bubbleStyle.shape
    ) {
        Text(
            text = content,
            color = bubbleStyle.textColor,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// **提取：时间戳**
@Composable
fun TimestampRow(timestamp: Long, isSentByMe: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = formatTimestamp(timestamp),
            style = MaterialTheme.typography.overline,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp

    val minute = 60.0 * 1000
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day
    val month = 30 * day
    val year = 365 * day

    return when {
        diff < minute -> "Just now"
        diff < hour -> "${(diff / minute).toInt()} minute(s) ago"
        diff < day -> "${(diff / hour).toInt()} hour(s) ago"
        diff < week -> "${(diff / day).toInt()} day(s) ago"
        diff < month -> "${(diff / week).toInt()} week(s) ago"
        diff < year -> "${(diff / month).toInt()} month(s) ago"
        else -> "${diff / year} year(s) ago"
    }
}
