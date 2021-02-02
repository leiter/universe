package com.together.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.firebase.ui.auth.AuthUI
import com.together.R


object AQ {

    private fun getConnectionManager(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun isConnectedTo(connectivityManager: ConnectivityManager, type: Int) : Boolean {
//        @deprecated Applications should instead use {@link NetworkCapabilities#hasCapability} or WIFI_SERVICE
        val ni = connectivityManager.activeNetworkInfo
        return ni !=null && ni.isConnected && ni.type ==  type //ConnectivityManager.TYPE_WIFI
    }

    fun hasInternetConnection(context: Context): Boolean {
        return isConnectedTo(getConnectionManager(context),ConnectivityManager.TYPE_WIFI) ||
                isConnectedTo(getConnectionManager(context),ConnectivityManager.TYPE_MOBILE)
    }

    fun getFirebaseUIStarter(): Intent {
        val providers = listOf(
            AuthUI.IdpConfig.AnonymousBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.AppTheme)
            .setAvailableProviders(providers).build()
    }

}