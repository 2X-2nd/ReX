package com.example.hellofigma.message

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hellofigma.message.model.Chat
import com.example.hellofigma.ui.theme.HelloFigmaTheme
import kotlinx.coroutines.delay

class MessageActivity : ComponentActivity() {
    private val viewModel by viewModels<ChatViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra("userId") ?: ""

        enableEdgeToEdge()
        setContent {
            HelloFigmaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // FullScreenScreen(this)
                    if (userId.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("not logged on!")
                        }
                    } else {
                        MessageList(
                            userId = userId,
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding).fillMaxSize(),
                            onBack = { finish() },
                            onChatClicked = { otherUserId, otherUserName ->
                                startActivity(
                                    Intent(this, ChatActivity::class.java)
                                        .putExtra("userId", userId)
                                        .putExtra("otherUserId", otherUserId)
                                        .putExtra("otherUserName", otherUserName)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageList(
    userId: String,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onChatClicked: (String, String) -> Unit
) {
    val chats by viewModel.chats.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.getChatList(userId)
            delay(6000)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.loadChats(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { innerPadding ->
            when (chats) {
                null -> {}
                emptyList<Chat>() -> {}
                else -> {
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(chats!!) { chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = { onChatClicked(chat.otherUserId, chat.otherUserName) }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ChatListItem(chat: Chat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = chat.otherUserName,
                style = MaterialTheme.typography.h6
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 靠左的文本
                Text(
                    text = chat.lastMessage,
                    style = MaterialTheme.typography.body2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 50.dp)
                )

                // 靠右的文本
                Text(
                    text = formatTimestamp(chat.timestamp),
                    style = MaterialTheme.typography.body2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
