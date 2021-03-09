package com.together.app

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.together.R
import com.together.base.*
import com.together.order.ProductsFragment
import com.together.repository.auth.FireBaseAuth
import com.together.utils.AQ
import com.together.utils.hasInternet
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val disposable = CompositeDisposable()

    companion object {

        const val LOGIN_REQUEST = 12

        private const val LOGIN_ACTION = ".action.login"

        fun startLogin(context: Context) {
            val i = Intent(context, MainActivity::class.java).apply {
                action = context.packageName + LOGIN_ACTION
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(i)
        }
    }

    private fun isTablet(){
        if(resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        isTablet()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
            supportFragmentManager,
            ProductsFragment(), ProductsFragment.TAG)
        )

        viewModel.loggedState.observe(this, {
            when (it) {

                is UiState.BaseAuth -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
                        supportFragmentManager,
                        ProductsFragment(), ProductsFragment.TAG)
                    )
                }

                is UiState.LoginRequired -> {
                    if(hasInternet()){
                        FireBaseAuth.loginAnonymously()
                    }
                    else {
                        val u = UiEvent.ReplaceFragment(supportFragmentManager,
                            NoInternetFragment(),NoInternetFragment.TAG)
                        MainMessagePipe.uiEvent.onNext(u)
                    }
                }
//                    startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)

                is UiState.LoggedOut -> {
                    if (!firstStart){
                        recreate()
                    }
                    firstStart = false
                }
            }
        })

    }

    private var firstStart = true

    override fun onBackPressed() {
        when (supportFragmentManager.backStackEntryCount) {
            0 -> moveTaskToBack(true)
            else -> super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.action == packageName + LOGIN_ACTION) {
//            MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
            startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
        }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }


}
