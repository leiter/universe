package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.jakewharton.picasso.OkHttp3Downloader
import com.jakewharton.rxbinding3.material.itemSelections
import com.jakewharton.rxbinding3.view.clicks
import com.squareup.picasso.Picasso
import com.together.R
import com.together.about.AboutFragment
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.create.CreateFragment
import com.together.loggedout.LoginFragment
import com.together.profile.ProfileFragment
import com.together.repository.Database
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getSingleExists
import com.together.utils.AQ
import com.together.utils.hideIme
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.sell.activity_main.*


class MainActivity : AppCompatActivity() {



    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

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
                            setLoggedIn(navigation_drawer, log_out)
                            disposable.add(setupDrawerNavigation(navigation_drawer, drawer_layout))
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
                                UiEvent.ReplaceFragment(supportFragmentManager,
                                    ProfileFragment(), ProfileFragment.TAG)
                            )

                        }
                    }, {
                        MainMessagePipe.uiEvent.onNext(
                            UiEvent.ShowToast(baseContext, R.string.developer_error_hint, Gravity.TOP)
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
            }
        })

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.action == packageName + LOGIN_ACTION) {
            startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
        } else {
            finish()
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

    private fun setupDrawerNavigation(navigationItemView: NavigationView, drawer: DrawerLayout): Disposable {

        return navigationItemView.itemSelections().subscribe {
            when (it.itemId) {
                R.id.drawer_nav_1 -> {

                }

                R.id.drawer_nav_4 -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.AddFragment(
                        supportFragmentManager,
                        AboutFragment(), AboutFragment.TAG))
                }

            }
            drawer.closeDrawers()
        }
    }

//    fun setLoggedOut(navigation_drawer: NavigationView, logOut : View) {
//        val head = navigation_drawer.getHeaderView(0)!!
//        head.findViewById<ImageView>(R.id.user_avatar).visibility = View.GONE
//        head.findViewById<TextView>(R.id.user_email).text = ""
//        head.findViewById<TextView>(R.id.user_name).text = ""
//        val logIn = head.findViewById<Button>(R.id.log_in)
//        logIn.visibility = View.VISIBLE
//        logOut.visibility = View.GONE
//        logIn.setOnClickListener {
//            MainMessagePipe.uiEvent.onNext(UiEvent.LogIn(logIn.context))
//        }
//    }

    private fun setLoggedIn(navigation_drawer: NavigationView, logOut: View) {
        val user = FireBaseAuth.getAuth()!!.currentUser!!
        val head = navigation_drawer.getHeaderView(0)!!
        val avatar = head.findViewById<ImageView>(R.id.user_avatar)
        head.findViewById<Button>(R.id.log_in).visibility = View.GONE
        avatar.visibility = View.VISIBLE
        logOut.visibility = View.VISIBLE
        head.findViewById<TextView>(R.id.user_email).text = user.email
        head.findViewById<TextView>(R.id.user_name).text = user.displayName

        if (user.photoUrl != null) {
            val p = Picasso.Builder(avatar.context).downloader(OkHttp3Downloader(avatar.context)).build()
            p.load(user.photoUrl).placeholder(R.drawable.ic_avatar_placeholder_24dp).into(avatar)
        }
    }


}
