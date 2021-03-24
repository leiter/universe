package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.work.*
import com.google.android.material.navigation.NavigationView
import com.jakewharton.rxbinding3.material.itemSelections
import com.squareup.picasso.Picasso
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.databinding.ActivityMainBinding
import com.together.profile.ProfileFragment.Companion.KEY_BACK_BUTTON
import com.together.repository.auth.FireBaseAuth
import com.together.utils.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

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
        setContentView(viewBinding.root)
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, Cre()).commit()
//        }

        NavigationUI.setupWithNavController(
            viewBinding.navigationView,
            findNavController(R.id.navigation_controller)
        )

        viewModel.loggedState.observe(this, {

            when (it) {
                is UiState.BaseAuth -> {
                    if (it.hasProfile) {

                        setLoggedIn(viewBinding.navigationView)
                        disposable.add(setupDrawerNavigation())
                        viewBinding.drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
                        if(!viewModel.isWorking) {
                            startWorkerManager()
                            viewModel.isWorking = true
                        }

                    } else {
                        viewBinding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                    }

                }

                is UiState.LoggedOut -> {
                    viewBinding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                    findNavController(R.id.navigation_controller)
                        .navigate(R.id.loginFragment)
                }
                else -> Log.d("MainActivity", "Not interested in all UiStates.")
            }
        })

        disposable.add(MainMessagePipe.uiEvent.subscribe {
            when (it) {
                is UiEvent.DrawerState -> {
                    if (it.gravity == Gravity.START) {
                        viewBinding.container.hideIme()
                        viewBinding.drawerLayout.openDrawer(viewBinding.navigationView)
                    } else viewBinding.drawerLayout.closeDrawers()
                }
                is UiEvent.LockDrawer -> {
                    viewBinding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                }
                is UiEvent.UnlockDrawer -> {
                    viewBinding.drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
                }
            }
        })
    }

    private fun startWorkerManager() {
        val w = WorkManager.getInstance(applicationContext)
        val c = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val r = OneTimeWorkRequestBuilder<SwitchWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(workDataOf(SwitchWorker.MODE to ACTION_START_SERVICE))
            .addTag("Marco")
            .setConstraints(c)
            .build()
        w.enqueueUniqueWork(ACTION_START_SERVICE,ExistingWorkPolicy.KEEP,r)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.action == packageName + LOGIN_ACTION) {
            startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
        }
        if (intent != null && intent.action == ACTION_SHOW_ORDER_FRAGMENT) {
            findNavController(R.id.navigation_controller)
                .navigate(R.id.showOrdersFragment)
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
                viewModel.loggedState.value = UiState.BaseAuth()
            } else {
                findNavController(R.id.navigation_controller).navigate(R.id.loginFragment)
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
            findNavController(R.id.navigation_controller).currentDestination!!.id ==
                    R.id.createFragment -> {
                moveTaskToBack(true)
                return
            }
            else -> super.onBackPressed()
        }
    }

    private fun setupDrawerNavigation(): Disposable {
        return viewBinding.navigationView.itemSelections().subscribe({
            when (it.itemId) {
                R.id.profileFragment -> {
                    findNavController(R.id.navigation_controller).navigate(
                        it.itemId,
                        bundleOf(KEY_BACK_BUTTON to true)
                    )
                    viewBinding.drawerLayout.closeDrawers()
                    return@subscribe
                }
                R.id.productViewsFragment -> {
                    val payload = ArrayList(viewModel.productList.value!!)
                    MainMessagePipe.transferCache.onNext(UiState.ProductList(payload))
                    findNavController(R.id.navigation_controller).navigate(
                        it.itemId
                    )
                    viewBinding.drawerLayout.closeDrawers()
                    return@subscribe
                }
            }
            NavigationUI.onNavDestinationSelected(it, findNavController(R.id.navigation_controller))
            viewBinding.drawerLayout.closeDrawers()
        }, { it.printStackTrace() })
    }

    private fun setLoggedIn(navigation_drawer: NavigationView) {
        val user = FireBaseAuth.getAuth().currentUser!!
        val head = navigation_drawer.getHeaderView(0)!!
        val avatar = head.findViewById<ImageView>(R.id.user_avatar)
        head.findViewById<Button>(R.id.log_in).visibility = View.GONE
        avatar.visibility = View.VISIBLE
        viewBinding.btnBottom.btnContainer.visibility = View.VISIBLE
        viewBinding.btnBottom.btnContainer.setOnClickListener {
            viewModel.prepareLogout()
            viewBinding.drawerLayout.closeDrawers()
            MainMessagePipe.uiEvent.onNext(UiEvent.LogOut)
        }
        head.findViewById<TextView>(R.id.user_email).text = user.email
        head.findViewById<TextView>(R.id.user_name).text = user.displayName
        user.photoUrl?.let {
            Picasso.with(this).load(user.photoUrl)
                .placeholder(R.drawable.ic_avatar_placeholder_24dp).into(avatar)
        }
    }


}
