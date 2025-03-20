package com.example.hellofigma

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Verify the loading indicator display
    @Test
    fun testLoadingState() {
        composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    // Test product pagination button
    @Test
    fun testProductPaginationButtonDisplay() {
        composeTestRule.onNodeWithText("Last Page").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next Page").assertIsDisplayed()
    }

    // Test the bottom navigation button
    @Test
    fun testBottomNavigationBarDisplay() {
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Categories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Messages").assertIsDisplayed()
        composeTestRule.onNodeWithText("User").assertIsDisplayed()
    }

    @Test
    fun testNextPage() {
        composeTestRule.onNodeWithText("Next Page").performClick()
    }

    @Test
    fun testLastPage() {
        composeTestRule.onNodeWithText("Last Page").performClick()
    }
}
