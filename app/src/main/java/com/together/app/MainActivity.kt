package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.clicks
import com.together.R
import com.together.order.main.ProductsFragment
import com.together.repository.auth.FirebaseAuth
import com.together.utils.AQ
import com.together.utils.hide
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val fire = FirebaseAuth

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProductsFragment()).commit()
        }

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.loggedState.observe(this, Observer {
            when (it) {

                is UiState.LOGGEDIN ->
                    presenter.setLoggedIn(navigation_drawer, log_out)

                is UiState.LOGGEDOUT -> {
                    presenter.setLoggedOut(navigation_drawer, log_out)
                }

            }

        })
        viewModel.loggedState.value = fire.isLoggedIn()
        disposable.add(presenter.setupDrawerNavigation(navigation_drawer, drawer_layout))
        disposable.add(presenter.setupBottomNavigation(navigation, supportFragmentManager))
        disposable.add(log_out.clicks().subscribe {
            drawer_layout.closeDrawers()
            MainMessagePipe.uiEvent.onNext(UiEvent.LogOut)
        })


        menu_start.setOnClickListener {
            drawer_layout.openDrawer(navigation_drawer)
            container.hide()
        }

        menu_end.setOnClickListener {

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
        disposable.clear()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.loggedState.value = UiState.LOGGEDIN
            }
        } else {
            moveTaskToBack(true)
        }
    }


}
