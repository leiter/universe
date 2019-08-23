package com.together.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.together.base.MainMessagePipe
import com.together.base.UiState
import com.together.repository.Result


object FireBaseAuth : FirebaseAuth.AuthStateListener {

    init {
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    var userProfile: Result.BuyerProfile = Result.BuyerProfile()


    fun getAuth(): FirebaseAuth? {
        return FirebaseAuth.getInstance()
    }
    fun isLoggedIn(): UiState {
        return when (getAuth()?.currentUser != null) {
            true -> UiState.BASE_AUTH
            else -> UiState.LOGGEDOUT
        }
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        val user = p0.currentUser
        if (user == null) {
            MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
        } else {
            MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn)
        }
    }

    fun logOut(){
        getAuth()?.signOut()
    }


}