package com.together.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.together.app.MainMessagePipe
import com.together.app.UiState
import com.together.repository.Result


object FirebaseAuth : FirebaseAuth.AuthStateListener {

    private val firebaseAuth = FirebaseAuth.getInstance()

    val fireUser: FirebaseUser? = firebaseAuth.currentUser

    init {
        firebaseAuth.addAuthStateListener(this)
    }

    fun isLoggedIn(): UiState {
        return when (firebaseAuth.currentUser != null) {
            true -> UiState.LOGGEDIN
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
        firebaseAuth.signOut()
    }


}