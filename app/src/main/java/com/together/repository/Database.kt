package com.together.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.together.repository.auth.FireBaseAuth

object Database {

    private const val ARTICLES = "articles"
    private const val SELLER_PROFILE = "seller_profile"
    private const val ORDERS = "orders"
    private const val CLIENTS = "buyer_profile"

    private fun fire() : FirebaseDatabase { return FirebaseDatabase.getInstance() }

    fun articles(): DatabaseReference = fire().reference.child(ARTICLES).child(FireBaseAuth.getAuth().uid!!)

    fun sellerProfile(sellerId: String, seller: Boolean = false): DatabaseReference {
        val result = fire().reference.child(SELLER_PROFILE)
        if(sellerId.isNotEmpty()) result.child(sellerId)
        return if (seller) result.child(FireBaseAuth.getAuth().uid!!) else result
    }
    fun connectedStatus() = fire().getReference(".info/connected")

    fun orders(): DatabaseReference = fire().reference.child(ORDERS).child(FireBaseAuth.getAuth().uid!!)
    fun providerArticles(providerId: String): DatabaseReference  = fire().reference.child(ARTICLES).child(providerId)
    fun buyer(): DatabaseReference = fire().reference.child(CLIENTS).child(FireBaseAuth.getAuth().uid!!)
    fun updateArticle(id: String): DatabaseReference = fire().reference.child(ARTICLES)
        .child(FireBaseAuth.getAuth().uid!!).child(id)



    init {
        fire().setPersistenceEnabled(true)
        orders().keepSynced(true)
        sellerProfile("").keepSynced(true)
    }
}