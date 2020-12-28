package com.together

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.together.base.MainMessagePipe
import com.together.base.UiState
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.TestData
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getCompletable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.debug.activity_test_container.*
import java.util.*

class TestContainerActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    val testData = TestDataHolder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_container)
        compositeDisposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is Result.LoggedIn -> {
                    testData.loginState = UiState.BASE_AUTH
                    loading(false)
                }
                is Result.LoggedOut -> {
                    testData.loginState = UiState.LOGGEDOUT
                    loading(false)
                }
            }
        })
        testData.loginState = FireBaseAuth.isLoggedIn()
        setupClicks()
    }

    private fun setupClicks() {
        login.setOnClickListener {
            loading(true)
            FireBaseAuth.loginWithCredentials(testData.emailAddress, testData.passWord)
        }
        create_account.setOnClickListener {
            loading(true)
            FireBaseAuth.createAccount(testData.emailAddress, testData.passWord)
        }
        logout.setOnClickListener {
            loading(true)
            FireBaseAuth.logOut()
        }
        upload_seller_profile.setOnClickListener { setupSellerProfile() }
        upload_products.setOnClickListener { uploadList() }
    }


    private fun uploadList() {

    }

    private fun setupSellerProfile() {
        loading(true)
        compositeDisposable.add(Database.profile()
            .setValue(TestData.sellerProfile).getCompletable().subscribe({ success ->
                if (success) {
                    loading(false)
                } else {
                    loading(false)
                }
            }, {
                loading(false)
                Toast.makeText(this, "EEEEEEEEEEEE", Toast.LENGTH_SHORT).show()
            })
        )
    }

    private fun loading(show: Boolean) {
        load_indicator.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}


data class TestDataHolder(
    var emailAddress: String = generateEmail(),
    var passWord: String = "12345678",
    var loginState: UiState = FireBaseAuth.isLoggedIn(),
//    var exception: Throwable =
)



fun generateEmail(): String {
    val leftLimit = 97
    val rightLimit = 122
    val targetLength = 10
    val random = Random()
    val stringBuilder = StringBuilder()

    for (i in 0 until targetLength) {
        val randomChar = leftLimit +
                (random.nextFloat() * (rightLimit - leftLimit + 1)).toInt()
        stringBuilder.append(randomChar.toChar())
    }
    return stringBuilder.append("@arcor.de").toString()
}