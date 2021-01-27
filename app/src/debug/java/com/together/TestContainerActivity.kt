package com.together

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.together.base.MainMessagePipe
import com.together.base.UiState
import com.together.base.UtilsActivity
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getSingle
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.debug.activity_test_container.*
import java.util.*

class TestContainerActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private val compositeDisposable = CompositeDisposable()

    val testData = TestDataHolder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_container)
        compositeDisposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            if (!testData.isGoogleAuth) {
                when (it) {
                    is Result.LoggedIn -> {
                        testData.loginState = UiState.BaseAuth(
                            UiState.BuyerProfile(isAnonymous = it.currentUser.isAnonymous))
                        loading(false)
                    }
                    is Result.LoggedOut -> {
                        testData.loginState = UiState.LOGGEDOUT
                        loading(false)
                    }
                    is Result.AccountDeleted -> {
                        testData.loginState = UiState.ACCOUNT_DELETED
                        loading(false)
                    }
                }
            }
        })
        FirebaseAuth.getInstance().addAuthStateListener(this)
        setupClicks()
    }

    private fun setupClicks() {
        login.setOnClickListener {
            loading(true)
            FireBaseAuth.loginWithEmailAndPassWord(testData.emailAddress, testData.passWord)
        }
        create_account.setOnClickListener {
            loading(true);
            FireBaseAuth.createAccountWithEmailAndPassword(testData.emailAddress, testData.passWord)
        }
        logout.setOnClickListener { loading(true); FireBaseAuth.logOut() }
        upload_seller_profile.setOnClickListener { setupSellerProfile() }
        upload_products.setOnClickListener { uploadList() }
        delete_user.setOnClickListener { loading(true); FireBaseAuth.deleteAccount() }
        login_google.setOnClickListener { loading(true); UtilsActivity.startGoogleSigning(this) }
    }

    private fun uploadList() {
        Observable.fromIterable(testData.productList).map{
            Observable.just(it).subscribeOn(Schedulers.io())
        }.flatMap { it.map { article ->
            Database.articles().push().setValue(article)} }
            .subscribe()

    }

    private fun setupSellerProfile() {
        loading(true)
        testData.sellerProfile.sellerId = FireBaseAuth.getAuth().currentUser!!.uid
        compositeDisposable.add(
            Database.sellerProfile("",true)
                .setValue(testData.sellerProfile)
                .getSingle().subscribe({ success ->
                    if (success) {
                        loading(false)
                    } else {
                        loading(false)
                    }
                }, {
                    loading(false)
                    it.printStackTrace()
                    Toast.makeText(this, "Exception happened", Toast.LENGTH_SHORT).show()
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

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if (p0.currentUser != null) {
            testData.loginState = UiState.BaseAuth(
                UiState.BuyerProfile(isAnonymous = p0.currentUser!!.isAnonymous))
        } else testData.loginState = UiState.LOGGEDOUT
        loading(false)
    }

}


data class TestDataHolder(
    var emailAddress: String = generateEmail(),
    var passWord: String = "12345678",
    var loginState: UiState = FireBaseAuth.isLoggedIn(),
    var isGoogleAuth: Boolean = false,
    var sellerProfile: Result.SellerProfile = TestData.sellerProfile,
    var productList: List<Result.Article> = TestData.articleList


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

