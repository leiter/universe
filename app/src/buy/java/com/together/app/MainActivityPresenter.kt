package com.together.app

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.jakewharton.picasso.OkHttp3Downloader
import com.jakewharton.rxbinding3.material.itemSelections
import com.squareup.picasso.Picasso
import com.together.R
import com.together.about.AboutFragment
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.order.ProductsFragment
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getSingle
import com.together.repository.storage.getSingleExists
import com.together.utils.hideIme
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class MainActivityPresenter(private val disposable: CompositeDisposable,
                            private val supportFragmentManager: FragmentManager) {

    private fun setupDrawerNavigation(navigationItemView: NavigationView): Disposable {

        return navigationItemView.itemSelections().subscribe {
            when (it.itemId) {

                R.id.menu_orders -> {
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

    fun setLoggedOut(navigation_drawer: NavigationView, logOut: View) {
        val head = navigation_drawer.getHeaderView(0)!!
        head.findViewById<ImageView>(R.id.user_avatar).visibility = View.GONE
        head.findViewById<ImageView>(R.id.user_avatar).setImageDrawable(null)
        head.findViewById<TextView>(R.id.user_email).text = ""
        head.findViewById<TextView>(R.id.user_name).text = ""
        val logIn = head.findViewById<Button>(R.id.log_in)
        logIn.visibility = View.VISIBLE
        logOut.visibility = View.GONE
        logIn.setOnClickListener {
            MainMessagePipe.uiEvent.onNext(UiEvent.LogIn(logIn.context))
        }

    }

    fun setLoggedIn(navigation_drawer: NavigationView, logOut: View) {

        val user = FireBaseAuth.getAuth()!!.currentUser!!

        disposable.add(Database.buyer().getSingleExists().flatMap {
                when (it) {
                    true -> Database.buyer().getSingle<Result.BuyerProfile>()
                    else -> Single.just(Unit)
                }

            }.subscribe({
                when (it) {
                    is Result.BuyerProfile -> {



                    }
                    is Unit -> {
                        val buyer = Result.BuyerProfile()
                    }

                }


            }, {
                Log.e("EEEEE", "For debugging", it);
            }
            )
        )


        setupDrawerNavigation(navigation_drawer)

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

    fun setupBottomNavigation(navigation: BottomNavigationView, fm: FragmentManager): Disposable {
        return navigation.itemSelections().subscribe {
            when (it.itemId) {
                R.id.navigation_home -> {
                    navigation.hideIme()
                    fm.beginTransaction()
                        .replace(R.id.container, ProductsFragment()).commit()
                }
//                R.id.navigation_notifications -> {
//                    fm.beginTransaction()
//                        .replace(R.id.container, AnyIdeaFragment()).commit()
//                }
            }

        }
    }


}