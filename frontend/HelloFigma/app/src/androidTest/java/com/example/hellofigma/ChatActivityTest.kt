package com.example.hellofigma

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hellofigma.message.ChatScreen
import com.example.hellofigma.message.ChatViewModel
import com.example.hellofigma.message.model.Message
import com.example.hellofigma.message.repository.NetworkResult
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @MockK
    private lateinit var mockViewModel: ChatViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true) // 使用 relaxed mock 避免严格检查

        // 配置 loadChatHistoryState 的模拟
        val mockLoadState = MutableStateFlow<NetworkResult<Unit>?>(null)
        every { mockViewModel.loadChatHistoryState } returns mockLoadState

        // 配置其他方法
        coEvery { mockViewModel.getMessages(any()) } returns flowOf(
            listOf(
                Message(
                    messageId = "1",
                    chatId = "chat1",
                    content = "Hello",
                    isSentByMe = false,
                    timestamp = System.currentTimeMillis()
                ),
                Message(
                    messageId = "2",
                    chatId = "chat1",
                    content = "Hi there!",
                    isSentByMe = true,
                    timestamp = System.currentTimeMillis()
                )
            )
        )

        coEvery { mockViewModel.resetLoadChatHistoryState() } just Runs
    }

    // Test message list
    @Test
    fun shouldDisplayMessages_whenViewModelProvidesThem() {
        composeTestRule.setContent {
            ChatScreen(
                userId = "user1",
                chatId = "chat1",
                otherUserId = "user2",
                otherUserName = "Test User",
                viewModel = mockViewModel,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
        Thread.sleep(2000)
        composeTestRule.onNodeWithText("Hi there!").assertIsDisplayed()
        Thread.sleep(2000)
    }

    // Test sending messages
    @Test
    fun shouldSendMessage_whenInputTextAndClickSend() {
        composeTestRule.setContent {
            ChatScreen(
                userId = "user1",
                chatId = "chat1",
                otherUserId = "user2",
                otherUserName = "Test User",
                viewModel = mockViewModel,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithTag("MessageInput")
            .performTextInput("Test message")

        Thread.sleep(2000)

        composeTestRule.onNodeWithTag("SendButton")
            .performClick()

        Thread.sleep(2000)

        coVerify {
            mockViewModel.sendMessage(
                chatId = "chat1",
                userId = "user1",
                otherUserId = "user2",
                message = "Test message"
            )
        }
    }
}
