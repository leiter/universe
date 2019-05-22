package com.together

import android.util.Log
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.together.app.MainMessagePipe
import com.together.repository.storage.Firebase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FireDatabaseTests {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.together", appContext.packageName)
//    }

    //    lateinit var pipe: PublishSubject<>
    lateinit var fire: Firebase

    var notNull: String? = null
    val thisIdleFunction = { notNull != null }

    @Before
    fun setup() {
        fire = Firebase()


    }

    @Test
    fun getArticleList() {
        val idlingResource = IdleMessager("getArticleList", thisIdleFunction)
        IdlingRegistry.getInstance().register(idlingResource)
        val app =
            FirebaseApp.initializeApp(InstrumentationRegistry
                .getInstrumentation().targetContext) as FirebaseApp
        fire.getSupplyList(FirebaseDatabase.getInstance(app).reference)
        MainMessagePipe.mainThreadMessage.subscribe(
            { Log.e("SUCCESS", "getArticleList") },
            {
                Log.e("FAILED", "getArticleList")
                notNull = ""

            }, {
                notNull = ""
            }
        )
    }

}




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