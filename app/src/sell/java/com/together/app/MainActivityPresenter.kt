package com.together.app

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
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
import com.together.create.CreateFragment
import com.together.order.ProductsFragment
import com.together.repository.auth.FireBaseAuth
import com.together.utils.hideIme
import io.reactivex.disposables.Disposable

class MainActivityPresenter(val view: MainActivityView) {

    fun setupDrawerNavigation(navigationItemView: NavigationView, drawer: DrawerLayout): Disposable {

        return navigationItemView.itemSelections().subscribe {
            when (it.itemId) {
                R.id.drawer_nav_1 -> {

                }

                R.id.drawer_nav_4 -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.AddFragment(
                        view.giveFragmentManager(),
                        AboutFragment(),AboutFragment.TAG))
                }

            }
            drawer.closeDrawers()
        }
    }

    fun setLoggedOut(navigation_drawer: NavigationView, logOut :View) {
        val head = navigation_drawer.getHeaderView(0)!!
        head.findViewById<ImageView>(R.id.user_avatar).visibility = View.GONE
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
                    fm.beginTransaction().replace(R.id.container, ProductsFragment()).commit()
                }

                R.id.navigation_dashboard -> {
                    fm.beginTransaction().replace(R.id.container, CreateFragment()).commit()
                }

//                R.id.navigation_notifications -> {
//                    fm.beginTransaction()
//                        .replace(R.id.container, AnyIdeaFragment()).commit()
//                }
            }

        }
    }

}