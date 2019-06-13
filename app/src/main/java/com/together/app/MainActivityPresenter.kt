package com.together.app

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.rxbinding3.material.itemSelections
import com.together.R
import io.reactivex.disposables.Disposable

class MainActivityPresenter  {

    fun setupBottomNavigation(navigationItemView: BottomNavigationView) : Disposable {
        return navigationItemView.itemSelections().subscribe {
            when(it.itemId){
                R.id.profile -> {

                }

            }
        }
    }



}