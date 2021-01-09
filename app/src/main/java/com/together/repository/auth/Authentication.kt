package com.together.repository.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

interface Authentication {

    fun createUser(userEmail: String, password: String)

    fun signIn(userEmail: String, password: String)

    fun logOut()

}

class AuthenticationImpl @Inject constructor(context: Context) : Authentication {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun createUser(userEmail: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener {
            }
    }

    override fun signIn(userEmail: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener {
        }
    }

    override fun logOut() {
        firebaseAuth.signOut()
    }




}

