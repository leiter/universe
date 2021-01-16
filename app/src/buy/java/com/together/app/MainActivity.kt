package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding3.view.clicks
import com.together.R
import com.together.about.AboutFragment
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.loggedout.LoginFragment
import com.together.order.ProductsFragment
import com.together.splash.SplashScreenFragment
import com.together.utils.AQ
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.buy.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
//        MainMessagePipe.uiEvent.onNext(UiEvent.AddFragment(
//            supportFragmentManager,
//            SplashScreenFragment(), AboutFragment.TAG))

        MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
            supportFragmentManager,
            ProductsFragment(), ProductsFragment.TAG)
        )



        viewModel.loggedState.observe(this, {
            when (it) {

                is UiState.BASE_AUTH -> {}
//                    MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
//                        supportFragmentManager,
//                        ProductsFragment(), ProductsFragment.TAG)
//                    )

                is UiState.LOGIN_REQUIRED -> startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)



                is UiState.LOGGEDOUT -> {
//                    MainMessagePipe.uiEvent.onNext(
//                        UiEvent.ReplaceFragment(supportFragmentManager, LoginFragment(), "LoginFragment")
//                    )
                }
            }
        })

        disposable.add(btn_log_out.clicks().subscribe {
            MainMessagePipe.uiEvent.onNext(UiEvent.LogOut)
        })

    }

    override fun onBackPressed() {
        when (supportFragmentManager.backStackEntryCount) {
            0 -> moveTaskToBack(true)
            else -> super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.action == packageName + LOGIN_ACTION) {
            startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                onBackPressed()
            }
        } else {
            moveTaskToBack(true)
        }
    }

}
