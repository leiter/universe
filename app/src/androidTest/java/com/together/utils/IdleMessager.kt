package com.together.utils

import android.view.View
import android.widget.FrameLayout
import androidx.test.espresso.IdlingResource
import com.together.R
import com.together.TestContainerActivity

class IdleMessager(val activity: TestContainerActivity) : IdlingResource {

    @Volatile
    var resourceCallback : IdlingResource.ResourceCallback? = null

    override fun getName(): String {
        return "tagName"
    }

    override fun isIdleNow(): Boolean {
        val amI = when(activity) {
            null -> false
            else -> activity.findViewById<FrameLayout>(R.id.load_indicator).visibility == View.GONE
        }
        if(amI) resourceCallback?.onTransitionToIdle()
        return amI
    }

    override fun registerIdleTransitionCallback(call: IdlingResource.ResourceCallback?) {
        resourceCallback = call
    }

}