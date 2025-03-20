package com.example.hellofigma

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.hellofigma.data.repository.DataStoreManager
import com.example.hellofigma.data.repository.LoginState
import com.example.hellofigma.data.repository.ProductRepository
import com.example.weather_dashboard.data.models.ItemResponse
import com.example.weather_dashboard.data.models.UserResponse
import com.example.hellofigma.di.DataStoreModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@UninstallModules(DataStoreModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ItemActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant()

    @Inject
    lateinit var repository: ProductRepository

    @BindValue
    @JvmField
    val dataStoreManager: DataStoreManager = mockk(relaxed = true)

    private val mockProduct = ItemResponse(
        id = "test123",
        title = "Test Product",
        description = "Test Description",
        price = 99.9,
        seller_id = "seller123",
        images = listOf("base64_image_data"),
        latitude = 0.0,
        longitude = 0.0,
        created_at = "2025-03-01"
    )

    private val mockUser = UserResponse(
        google_id = "seller123",
        email = "seller@test.com",
        username = "SellerUser",
        preferences = emptyList(),
        latitude = 1.0,
        longitude = 1.0
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        coEvery { dataStoreManager.loginState } returns flowOf(
            LoginState(true, "other123", "user@test.com", "OtherUser")
        )

        hiltRule.inject()
        Intents.init()
        clearAllMocks()
    }

    @After
    fun cleanup() {
        Intents.release()
    }

    private fun createIntent(): Intent {
        return Intent(ApplicationProvider.getApplicationContext(), ItemActivity::class.java).apply {
            putExtra("id", mockProduct.id)
            putExtra("title", mockProduct.title)
            putExtra("description", mockProduct.description)
            putExtra("price", mockProduct.price)
            putExtra("seller_id", mockProduct.seller_id)
        }
    }

    // Verify basic information
    @Test
    fun shouldDisplayProductDetails() {
        ActivityScenario.launch<ItemActivity>(createIntent())

        // Verify basic information
        Espresso.onView(withId(R.id.productName)).check(matches(withText(mockProduct.title)))
        Espresso.onView(withId(R.id.productDescription)).check(matches(withText(mockProduct.description)))
        Espresso.onView(withId(R.id.productPrice)).check(matches(withText("99.9")))

        Thread.sleep(1000)
        Espresso.onView(withId(R.id.productImage)).check(matches(isDisplayed()))
    }

    // Testing has no authority to delete products
    @Test
    fun shouldShowPermissionDeniedWhenNotSeller() {
        coEvery { dataStoreManager.loginState } returns flowOf(
            LoginState(true, "other123", "user@test.com", "OtherUser")
        )

        ActivityScenario.launch<ItemActivity>(createIntent())
        Espresso.onView(withId(R.id.remove)).perform(click())
        Espresso.onView(withId(R.id.remove)).check(matches(withContentDescription("You do not have permission to delete")))
    }

    // The last product test was successful
    @Test
    fun shouldCloseActivityWhenDeleteSuccess() {
        coEvery { dataStoreManager.loginState } returns flowOf(
            LoginState(true, mockProduct.seller_id, "seller@test.com", "SellerUser")
        )

        val scenario = ActivityScenario.launch<ItemActivity>(createIntent())
        Espresso.onView(withId(R.id.remove)).perform(click())
        scenario.moveToState(Lifecycle.State.DESTROYED)
        assertTrue("Activity Destroyed", scenario.state == Lifecycle.State.DESTROYED)
    }

    // Test login user can open chat activity
    @Test
    fun shouldNavigateToChatWhenLoggedIn() {
        coEvery { dataStoreManager.loginState } returns flowOf(
            LoginState(true, "buyer123", "buyer@test.com", "BuyerUser")
        )

        ActivityScenario.launch<ItemActivity>(createIntent())
        Espresso.onView(withId(R.id.iWantItButton)).perform(click())
    }

    // Users who are not logged in are not allowed to open the chat
    @Test
    fun shouldShowToastWhenNotLoggedIn() {
        coEvery { dataStoreManager.loginState } returns flowOf(LoginState(false, "", "", ""))

        ActivityScenario.launch<ItemActivity>(createIntent())
        Espresso.onView(withId(R.id.iWantItButton)).perform(click())
        Espresso.onView(withId(R.id.iWantItButton)).check(matches(withContentDescription("You haven't logged in yet")))
    }
}
