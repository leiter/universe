package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.clicks
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.create.CreateFragment
import com.together.loggedout.LoginFragment
import com.together.profile.ProfileFragment
import com.together.repository.Database
import com.together.repository.storage.getSingleExists
import com.together.utils.AQ
import com.together.utils.hideIme
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.sell.activity_main.*


class MainActivity : AppCompatActivity(), MainActivityView {

    override fun giveFragmentManager(): FragmentManager {
        return supportFragmentManager!!
    }

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private val disposable = CompositeDisposable()

    private val presenter: MainActivityPresenter by lazy { MainActivityPresenter(this) }

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

        viewModel.loggedState.observe(this, Observer {
            when (it) {

                is UiState.BASE_AUTH -> {
                    Database.profile().getSingleExists().subscribe({ exists ->
                        if (exists) {
                            presenter.setLoggedIn(navigation_drawer, log_out)
                            disposable.add(presenter.setupDrawerNavigation(navigation_drawer, drawer_layout))
                            MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
                                supportFragmentManager,CreateFragment(),CreateFragment.TAG))
//                            disposable.add(presenter.setupBottomNavigation(navigation, supportFragmentManager))
                            disposable.add(
                                log_out.clicks().subscribe {
                                    drawer_layout.closeDrawers()
                                    MainMessagePipe.uiEvent.onNext(UiEvent.LogOut)
                                })
                        } else {
                            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                            MainMessagePipe.uiEvent.onNext(
                                UiEvent.ReplaceFragment(supportFragmentManager, ProfileFragment(), ProfileFragment.TAG)
                            )

                        }
                    }, {
                        MainMessagePipe.uiEvent.onNext(
                            UiEvent.ShowToast(baseContext, R.string.developer, Gravity.TOP)
                        )

                    })
                }

                is UiState.LOGGEDOUT -> {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    MainMessagePipe.uiEvent.onNext(
                        UiEvent.ReplaceFragment(supportFragmentManager, LoginFragment(), "wer")
                    )
//                    presenter.setLoggedOut(navigation_drawer, log_out)
                }

            }
        })

        disposable.add(MainMessagePipe.uiEvent.subscribe {
            when (it) {
                is UiEvent.DrawerState -> {
                    if (it.gravity == Gravity.START) {
                        container.hideIme()
                        drawer_layout.openDrawer(navigation_drawer)
                    } else drawer_layout.closeDrawers()
                }
                is UiEvent.LockDrawer -> {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                is UiEvent.UnlockDrawer -> {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
//                is UiEvent.ReplaceFrag -> {
//                    supportFragmentManager.beginTransaction()
//                        .add(R.id.container,it.fragment,it.tag).commit()
//                }
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
//                viewModel.loggedState.value = UiState.BASE_AUTH
            } else {
                MainMessagePipe.uiEvent.onNext(
                    UiEvent.ReplaceFragment(supportFragmentManager, LoginFragment(), "wer")
                )
            }
        } else {
            moveTaskToBack(true)
        }
    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> {
                drawer_layout.closeDrawer(GravityCompat.START)
                return
            }
            supportFragmentManager.backStackEntryCount == 0 -> {
                moveTaskToBack(true)
                container.clearFocus()
            }
            else -> super.onBackPressed()
        }
    }

}
