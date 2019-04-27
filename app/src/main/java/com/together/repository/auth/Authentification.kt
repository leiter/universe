package com.together.repository.auth

interface Authentification {

    fun createUser(userEmail:String, password:String)

    fun signIn(userEmail:String, password:String)

    fun logOut()

}

