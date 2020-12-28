package com.together.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.platform.app.ActivityLifecycleTimeout
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.together.R
import com.together.TestContainerActivity
import junit.extensions.ActiveTestSuite
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import java.lang.Exception


@RunWith(AndroidJUnit4::class)
abstract class BaseTest {

    @Volatile
    lateinit var waitForIt: IdleMessager

    @get:Rule
    var runTimeRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @get:Rule
    var activityRule = ActivityTestRule(TestContainerActivity::class.java,true)

    @Before
    fun setup() {
        waitForIt = IdleMessager(activityRule.activity)
        IdlingRegistry.getInstance().register(waitForIt)
    }

    @After
    fun tearDown(){
        IdlingRegistry.getInstance().unregister(waitForIt)
    }

    fun login(){
        Espresso.onView(withId(R.id.login)).perform(click())
    }
    fun logout(){
        Espresso.onView(withId(R.id.logout)).perform(click())
    }
}

inline fun <reified  T : Activity> getCurrentActivity() : T {
    var currentActivity = emptyArray<T>()
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        val resumedActivity = ActivityLifecycleMonitorRegistry.getInstance()
            .getActivitiesInStage(Stage.RESUMED)
        if (resumedActivity.iterator().hasNext()){
            currentActivity = arrayOf(resumedActivity.iterator().next() as T)
        }
    }
    if (currentActivity.size==1)
        return currentActivity[0]
    else throw IllegalStateException("Could not determine current activity.")
}