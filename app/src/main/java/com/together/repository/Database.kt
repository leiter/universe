package com.together.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.together.BuildConfig
import com.together.repository.auth.FireBaseAuth

object Database {

    private const val ARTICLES = "articles"
    private const val SELLER_PROFILE = "seller_profile"
    private const val ORDERS = "orders"
    private const val CLIENTS = "buyer_profile"
    private const val STORAGE_PREFIX = "images/"

    private fun fire(): FirebaseDatabase { return FirebaseDatabase.getInstance() }

    fun articles(): DatabaseReference =
        fire().reference.child(ARTICLES).child(FireBaseAuth.getAuth().uid!!)

    fun sellerProfile(sellerId: String = "", seller: Boolean = false): DatabaseReference {
        val result = fire().reference.child(SELLER_PROFILE)
        if (sellerId.isNotEmpty()) result.child(sellerId)
        return if (seller) result.child(FireBaseAuth.getAuth().uid!!) else result
    }

    fun connectedStatus() = fire().getReference(".info/connected")

    fun orderSeller(sellerId: String): DatabaseReference = fire().reference.child(ORDERS)
        .child(sellerId)

    fun nextOrders(date: String) = fire().reference.child(ORDERS)
        .child(FireBaseAuth.getAuth().uid!!)
        .child(date).orderByChild("pickUpDate")

    fun providerArticles(providerId: String): DatabaseReference =
        fire().reference.child(ARTICLES).child(providerId)

    fun buyer(): DatabaseReference =
        fire().reference.child(CLIENTS).child(FireBaseAuth.getAuth().uid!!)

    fun storage(filename: String = ""): StorageReference {
        val path = if (filename == "") "$STORAGE_PREFIX${System.currentTimeMillis()}_ttt.jpeg"
        else filename
        return FirebaseStorage.getInstance().reference.child(path)
    }

    init {
        fire().setPersistenceEnabled(true)
    }

}
