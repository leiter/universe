package com.together.base

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.together.R
import com.together.utils.BaseTest
import org.junit.Test

class ProfileDataTests  : BaseTest() {
    @Test
    fun uploadEricProfile() {
        Espresso.onView(ViewMatchers.withId(R.id.upload_seller_profile))
            .perform(ViewActions.click())
    }
}