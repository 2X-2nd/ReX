package com.example.hellofigma

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PostActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // Automatically grant location permissions
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun cleanup() {
        Intents.release()
    }

    @Test
    fun testActivityLaunch() {
        ActivityScenario.launch(PostActivity::class.java)

        Espresso.onView(withId(R.id.etTitle)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.etDescription)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.etPrice)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.radioYourLocation)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.radioWarehouse)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.radioManualAddress)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.btnPost)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.btnSelectLocation)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.tvUserLocation)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.etManualAddress)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.btnAddImage)).check(matches(isDisplayed()))
    }

    @Test
    fun testFormValidation() {
        ActivityScenario.launch(PostActivity::class.java)

        // Verify error prompts that have not been entered
        Espresso.onView(withId(R.id.btnPost)).perform(click())
        Espresso.onView(withId(R.id.etTitle)).check(matches(hasErrorText("Title is required")))

        Espresso.onView(withId(R.id.etTitle)).perform(clearText(), typeText("Bike"), closeSoftKeyboard())
        Espresso.onView(withId(R.id.btnPost)).perform(click())
        Espresso.onView(withId(R.id.etDescription)).check(matches(hasErrorText("Description is required")))

        Espresso.onView(withId(R.id.etDescription)).perform(clearText(), typeText("Test Description"), closeSoftKeyboard())
        Espresso.onView(withId(R.id.btnPost)).perform(click())
        Espresso.onView(withId(R.id.etPrice)).check(matches(hasErrorText("Price is required")))

        Espresso.onView(withId(R.id.etPrice)).perform(clearText(), typeText("10"), closeSoftKeyboard())
        Espresso.onView(withId(R.id.btnPost)).perform(click())
        Espresso.onView(withId(R.id.etTitle)).check(matches(hasErrorText("Image is required")))
    }
}