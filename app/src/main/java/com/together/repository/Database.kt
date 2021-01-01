package com.together.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.together.repository.auth.FireBaseAuth

object Database {

    private fun fire() : FirebaseDatabase{
       return FirebaseDatabase.getInstance()
    }

    init {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    private const val ARTICLES = "articles"
    private const val PROFILE = "profile"
    private const val ORDERS = "orders"
    private const val CLIENTS = "clients"

    fun articles(): DatabaseReference = fire().reference.child(ARTICLES).child(FireBaseAuth.getAuth()!!.uid!!)
    fun updateArticle(id: String): DatabaseReference = fire().reference.child(ARTICLES)
        .child(FireBaseAuth.getAuth()!!.uid!!).child(id)
    fun profile(): DatabaseReference = fire().reference.child(PROFILE).child(FireBaseAuth.getAuth()!!.uid!!)
    fun orders(): DatabaseReference = fire().reference.child(ORDERS).child(FireBaseAuth.getAuth()!!.uid!!)
    fun buyer(): DatabaseReference = fire().reference.child(CLIENTS).child(FireBaseAuth.getAuth()!!.uid!!)
    fun providerArticles(providerId: String): DatabaseReference  = fire().reference.child(ARTICLES).child(providerId)


}