package com.together.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.together.repository.auth.FireBaseAuth

object Database {

    private fun fire() : FirebaseDatabase { return FirebaseDatabase.getInstance() }

    init {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    private const val ARTICLES = "articles"
    private const val SELLER_PROFILE = "seller_profile"
    private const val ORDERS = "orders"
    private const val CLIENTS = "clients"

    fun articles(): DatabaseReference = fire().reference.child(ARTICLES).child(FireBaseAuth.getAuth()!!.uid!!)

    fun updateArticle(id: String): DatabaseReference = fire().reference.child(ARTICLES)
        .child(FireBaseAuth.getAuth()!!.uid!!).child(id)

    fun sellerProfile(seller: Boolean = false): DatabaseReference {
        val result = fire().reference.child(SELLER_PROFILE)
        return if (seller) result.child(FireBaseAuth.getAuth()!!.uid!!) else result
    }

    fun orders(): DatabaseReference = fire().reference.child(ORDERS).child(FireBaseAuth.getAuth()!!.uid!!)
    fun buyer(): DatabaseReference = fire().reference.child(CLIENTS).child(FireBaseAuth.getAuth()!!.uid!!)
    fun providerArticles(providerId: String): DatabaseReference  = fire().reference.child(ARTICLES).child(providerId)


}