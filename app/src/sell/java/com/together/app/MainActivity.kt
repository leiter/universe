package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.clicks
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.profile.ProfileFragment
import com.together.repository.Database
import com.together.repository.storage.getSingleExists
import com.together.utils.AQ
import com.together.utils.hideIme
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private val disposable = CompositeDisposable()

    private val presenter = MainActivityPresenter()

    companion object {

        const val LOGIN_REQUEST = 12

        private val LOGIN_ACTION = ".action.login"

        fun startLogin(context: Context) {
            val i = Intent(context, MainActivity::class.java).apply {
                action = context.packageName + LOGIN_ACTION
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(i)
        }
        fun reStart(context: Context) {
            val i = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, ProductsFragment()).commit()
//        }

        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)


        viewModel.loggedState.observe(this, Observer {
            when (it) {

                is UiState.BASE_AUTH -> {

//                    Handler().postDelayed({ loading_indicator.hide() },1500)
                    Database.profile().getSingleExists().subscribeBy({
                        MainMessagePipe.uiEvent.onNext(
                            UiEvent.ShowToast(baseContext, R.string.developer, Gravity.TOP)
                        )

                    }, { exists ->
                        if (exists) {
                            presenter.setLoggedIn(navigation_drawer, log_out)
                            disposable.add(presenter.setupDrawerNavigation(navigation_drawer, drawer_layout))
                            disposable.add(presenter.setupBottomNavigation(navigation, supportFragmentManager))
                            disposable.add(
                                log_out.clicks().subscribe {
                                    drawer_layout.closeDrawers()
                                    MainMessagePipe.uiEvent.onNext(UiEvent.LogOut)
                                })
                        } else {
                            MainMessagePipe.uiEvent.onNext(
                                UiEvent.ReplaceFragment(supportFragmentManager, ProfileFragment(), ProfileFragment.TAG)
                            )

                        }
                    })
                }

                is UiState.LOGGEDOUT -> {
                    presenter.setLoggedOut(navigation_drawer, log_out)
                }
            }
        })



        disposable.add(MainMessagePipe.uiEvent.subscribe {
            when (it) {
                is UiEvent.DrawerState -> if (it.gravity == Gravity.START) {
                    container.hideIme()
                    drawer_layout.openDrawer(navigation_drawer)
                } else drawer_layout.closeDrawers()
            }
        })

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.action == packageName + LOGIN_ACTION) {
            startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
        } else {
            finish()
            startActivity(intent)
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
                viewModel.loggedState.value = UiState.BASE_AUTH
            }
        } else {
            moveTaskToBack(true)
        }
    }

}
