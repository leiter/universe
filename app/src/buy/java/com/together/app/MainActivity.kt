package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.clicks
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.utils.AQ
import com.together.utils.hideIme
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.buy.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val disposable = CompositeDisposable()

    private val presenter : MainActivityPresenter by lazy {
        MainActivityPresenter(disposable, supportFragmentManager)
    }

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.loggedState.observe(this, Observer {
            when (it) {

                is UiState.BASE_AUTH ->
                    presenter.setLoggedIn(navigation_drawer, log_out)

                is UiState.LOGGEDOUT -> {
                    presenter.setLoggedOut(navigation_drawer, log_out)
                }
            }
        })

        disposable.add(log_out.clicks().subscribe {
            drawer_layout.closeDrawers()
            MainMessagePipe.uiEvent.onNext(UiEvent.LogOut)
        })

        disposable.add(MainMessagePipe.uiEvent.subscribe {
            when(it) {
                is UiEvent.OpenDrawer -> {
                    container.hideIme()
                    drawer_layout.openDrawer(navigation_drawer)
                }
                is UiEvent.CloseDrawer -> {
                    drawer_layout.closeDrawers()
                }
            }
        })

    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> {
                drawer_layout.closeDrawer(GravityCompat.START)
                return
            }
            supportFragmentManager.backStackEntryCount == 0 -> moveTaskToBack(true)
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

            }
        } else {
            moveTaskToBack(true)
        }
    }

}
