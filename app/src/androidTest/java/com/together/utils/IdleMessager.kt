package com.together.utils

import androidx.test.espresso.IdlingResource

class IdleMessager(val tagName: String, val idleFunction: () -> Boolean) : IdlingResource {

    private var callback: IdlingResource.ResourceCallback? = null

    override fun getName(): String {
        return tagName
    }

    override fun isIdleNow(): Boolean {
        val idle = idleFunction()
        if (idle) callback?.onTransitionToIdle()
        return idle
    }

    override fun registerIdleTransitionCallback(call: IdlingResource.ResourceCallback?) {
        callback = call
    }

}