package com.together.app

import androidx.fragment.app.FragmentManager


interface MainActivityView {
//    fun getDrawer(): DrawerLayout
//    fun getDrawerNavigationView(): NavigationView
//    fun getContainerView(): FrameLayout
//    fun getLogoutButton(): View
    fun giveFragmentManager() : FragmentManager
}