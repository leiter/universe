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
import com.together.databinding.ActivityMainBinding
import com.together.loggedout.LoginFragment
import com.together.profile.ProfileFragment
import com.together.repository.Database
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getSingleExists
import com.together.utils.AQ
import com.together.utils.hideIme
import com.together.utils.viewBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private val viewBinding : ActivityMainBinding by viewBinding (ActivityMainBinding::inflate)

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
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, ProductsFragment()).commit()
//        }

        viewModel.loggedState.observe(this, {
            when (it) {

                is UiState.BaseAuth -> {
                    Database.sellerProfile("", true).getSingleExists().subscribe({ exists ->
                        if (exists) {
                            setLoggedIn(viewBinding.navigationDrawer)
                            disposable.add(setupDrawerNavigation())
                            MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
                                supportFragmentManager, CreateFragment(),CreateFragment.TAG))
                            disposable.add(
                                viewBinding.btnBottom.logOut.clicks().subscribe {
                                    viewBinding.drawerLayout.closeDrawers()
                                    MainMessagePipe.uiEvent.onNext(UiEvent.LogOut)
                                })
                        } else {
                            viewBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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

                is UiState.LoggedOut -> {
                    viewBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    MainMessagePipe.uiEvent.onNext(
                        UiEvent.ReplaceFragment(supportFragmentManager, LoginFragment(), "wer")
                    )
                }
            }
        })

        disposable.add(MainMessagePipe.uiEvent.subscribe {
            when (it) {
                is UiEvent.DrawerState -> {
                    if (it.gravity == Gravity.START) {
                        viewBinding.container.hideIme()
                        viewBinding.drawerLayout.openDrawer(viewBinding.navigationDrawer)
                    } else viewBinding.drawerLayout.closeDrawers()
                }
                is UiEvent.LockDrawer -> {
                    viewBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                is UiEvent.UnlockDrawer -> {
                    viewBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
        })

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
            viewBinding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
                return
            }
            supportFragmentManager.backStackEntryCount == 0 -> {
                moveTaskToBack(true)
                viewBinding.container.clearFocus()
            }
            else -> super.onBackPressed()
        }
    }

    private fun setupDrawerNavigation(): Disposable {

        return viewBinding.navigationDrawer.itemSelections().subscribe {
            when (it.itemId) {
                R.id.drawer_nav_1 -> {


                }

                R.id.drawer_nav_4 -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
                        supportFragmentManager,
                        AboutFragment(), AboutFragment.TAG))
                }

            }
            viewBinding.drawerLayout.closeDrawers()
        }
    }

    private fun setLoggedIn(navigation_drawer: NavigationView) {
        val user = FireBaseAuth.getAuth().currentUser!!
        val head = navigation_drawer.getHeaderView(0)!!
        val avatar = head.findViewById<ImageView>(R.id.user_avatar)
        head.findViewById<Button>(R.id.log_in).visibility = View.GONE
        avatar.visibility = View.VISIBLE
        viewBinding.btnBottom.logOut.visibility = View.VISIBLE
        head.findViewById<TextView>(R.id.user_email).text = user.email
        head.findViewById<TextView>(R.id.user_name).text = user.displayName

        user.photoUrl?.let {
            val p = Picasso.Builder(avatar.context).downloader(OkHttp3Downloader(avatar.context)).build()
            p.load(user.photoUrl).placeholder(R.drawable.ic_avatar_placeholder_24dp).into(avatar)
        }
    }


}
