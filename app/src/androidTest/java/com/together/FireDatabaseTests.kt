package com.together

import android.util.Log
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.together.app.MainMessagePipe
import com.together.repository.storage.FireData
import com.together.utils.IdleMessager
import com.together.utils.TestData
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
    lateinit var fire: FireData

    val app = FirebaseApp.initializeApp(
        InstrumentationRegistry.getInstrumentation()
            .targetContext
    )

    var notNull: String? = null
    val thisIdleFunction = { notNull != null }

    @Before
    fun setup() {
        fire = FireData(
//            FirebaseDatabase.getInstance(app!!).reference
        )


    }

    @Test
    fun getArticleList() {
        val idlingResource = IdleMessager("getArticleList", thisIdleFunction)
        IdlingRegistry.getInstance().register(idlingResource)

//        fire.getSupplyList(FirebaseDatabase.getInstance(app!!).reference)
//        MainMessagePipe.mainThreadMessage.subscribe(
//            { Log.e("SUCCESS", "getArticleList") },
//            {
//                Log.e("FAILED", "getArticleList")
//                notNull = ""
//
//            }, {
//                notNull = ""
//            }
//        )
    }

    @Test
    fun createArticleList() {
        val idlingResource = IdleMessager("createArticleList", thisIdleFunction)
        IdlingRegistry.getInstance().register(idlingResource)

        val a = TestData.articleList[0]
        fire.createArticle(FirebaseDatabase.getInstance(app!!).reference, a)
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




