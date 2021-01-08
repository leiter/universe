package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.jakewharton.rxbinding3.material.itemSelections
import com.jakewharton.rxbinding3.view.clicks
import com.together.R

import com.together.about.AboutFragment
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.loggedout.LoginFragment
import com.together.order.ProductsFragment
import com.together.utils.AQ
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.buy.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val disposable = CompositeDisposable()

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
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.loggedState.observe(this, Observer {
            when (it) {

                is UiState.BASE_AUTH ->
                    MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
                        supportFragmentManager,
                        ProductsFragment(), ProductsFragment.TAG)
                    )
//                    setLoggedIn(navigation_drawer, log_out)

                is UiState.LOGGEDOUT -> {
                    setLoggedOut(navigation_drawer, btn_log_out)
                }
            }
        })

        disposable.add(btn_log_out.clicks().subscribe {
            drawer_layout.closeDrawers()
            MainMessagePipe.uiEvent.onNext(UiEvent.LogOut)
        })

//        disposable.add(MainMessagePipe.uiEvent.subscribe {
//            when(it) {
//                is UiEvent.OpenDrawer -> {
//                    container.hideIme()
//                    drawer_layout.openDrawer(navigation_drawer)
//                }
//                is UiEvent.CloseDrawer -> {
//                    drawer_layout.closeDrawers()
//                }
//            }
//        })

    }

    private fun setupDrawerNavigation(navigationItemView: NavigationView): Disposable {

        return navigationItemView.itemSelections().subscribe {
            when (it.itemId) {

                R.id.menu_order -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
                        supportFragmentManager,
                        ProductsFragment(), ProductsFragment.TAG)
                    )
                }


                R.id.licenses -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.AddFragment(
                        supportFragmentManager,
                        AboutFragment(), AboutFragment.TAG)
                    )
                }

            }
            MainMessagePipe.uiEvent.onNext(UiEvent.CloseDrawer)
        }
    }


    fun setLoggedIn(navigation_drawer: NavigationView, logOut: View) {
        MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
            supportFragmentManager,
            ProductsFragment(), ProductsFragment.TAG)
        )

//        val user = FireBaseAuth.getAuth()!!.currentUser!!
//        disposable.add(Database.buyer().getSingleExists().flatMap {
//            when (it) {
//                true -> Database.buyer().getSingle<Result.BuyerProfile>()
//                else -> Single.just(Result.Empty)
//            }
//
//        }.subscribe({
//            when (it) {
//                is Result.BuyerProfile -> {
//
//
//                }
//                is Result.Empty -> {
//                    val buyer = Result.BuyerProfile()
//
//                }
//
//            }
//        }, {
//            Log.e("EEEEE", "For debugging", it);
//        }))
//
//        setupDrawerNavigation(navigation_drawer)
//
//        val head = navigation_drawer.getHeaderView(0)!!
//        val avatar = head.findViewById<ImageView>(R.id.user_avatar)
//        head.findViewById<Button>(R.id.log_in).visibility = View.GONE
//        avatar.visibility = View.VISIBLE
//        logOut.visibility = View.VISIBLE
//        head.findViewById<TextView>(R.id.user_email).text = user.email
//        head.findViewById<TextView>(R.id.user_name).text = user.displayName
//
//        if (user.photoUrl != null) {
//            val p = Picasso.Builder(avatar.context).downloader(OkHttp3Downloader(avatar.context)).build()
//            p.load(user.photoUrl).placeholder(R.drawable.ic_avatar_placeholder_24dp).into(avatar)
//        }
    }


    fun setLoggedOut(navigation_drawer: NavigationView, logOut: View) {
        MainMessagePipe.uiEvent.onNext(
            UiEvent.ReplaceFragment(supportFragmentManager, LoginFragment(), "wer")
        )
//        val head = navigation_drawer.getHeaderView(0)!!
//        head.findViewById<ImageView>(R.id.user_avatar).visibility = View.GONE
//        head.findViewById<ImageView>(R.id.user_avatar).setImageDrawable(null)
//        head.findViewById<TextView>(R.id.user_email).text = ""
//        head.findViewById<TextView>(R.id.user_name).text = ""
//        val logIn = head.findViewById<Button>(R.id.log_in)
//        logIn.visibility = View.VISIBLE
//        logOut.visibility = View.GONE
//        logIn.setOnClickListener {
//            MainMessagePipe.uiEvent.onNext(UiEvent.LogIn(logIn.context))
//        }



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
