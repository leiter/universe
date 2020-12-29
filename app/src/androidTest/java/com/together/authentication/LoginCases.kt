package com.together.authentication

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.together.R
import com.together.base.UiState
import com.together.utils.BaseTest
import junit.framework.Assert.assertEquals
import org.junit.Test

class LoginCases : BaseTest() {

    @Test
    fun createAccount() {
        val testData = activityRule.activity.testData
        Espresso.onView(withId(R.id.create_account)).perform(click())
        assertEquals(testData.loginState,UiState.BASE_AUTH)
        Espresso.onView(withId(R.id.logout)).perform(click())
        assertEquals(testData.loginState,UiState.LOGGEDOUT)
        Espresso.onView(withId(R.id.login)).perform(click())
        assertEquals(testData.loginState,UiState.BASE_AUTH)
        Espresso.onView(withId(R.id.delete_user)).perform(click())
    }

    
}