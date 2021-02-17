package com.together.base

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.together.R
import com.together.utils.BaseTest
import org.junit.Test

class ProductDataTests : BaseTest() {

    @Test
    fun uploadProductsForMmaLeiter(){

        val testData = activityRule.activity.testData
        Espresso.onView(ViewMatchers.withId(R.id.logout)).perform(ViewActions.click())
        testData.isGoogleAuth = true
        Espresso.onView(ViewMatchers.withId(R.id.login_google)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.upload_products)).perform(ViewActions.click())
    }
}