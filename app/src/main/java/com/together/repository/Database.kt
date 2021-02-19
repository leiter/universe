package com.together.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.together.repository.auth.FireBaseAuth

object Database {

    private const val ARTICLES = "articles"
    private const val SELLER_PROFILE = "seller_profile"
    private const val ORDERS = "orders"
    private const val ORDER_IDS = "order_ids"
    private const val CLIENTS = "buyer_profile"
    private const val STORAGE_PREFIX = "images/"

    private fun fire() : FirebaseDatabase { return FirebaseDatabase.getInstance() }

    fun articles(): DatabaseReference = fire().reference.child(ARTICLES).child(FireBaseAuth.getAuth().uid!!)

    fun sellerProfile(sellerId: String = "", seller: Boolean = false): DatabaseReference {
        val result = fire().reference.child(SELLER_PROFILE)
        if(sellerId.isNotEmpty()) result.child(sellerId)
        return if (seller) result.child(FireBaseAuth.getAuth().uid!!) else result
    }

    fun connectedStatus() = fire().getReference(".info/connected")

    fun orders(): DatabaseReference = fire().reference.child(ORDERS).child(FireBaseAuth.getAuth().uid!!)

    fun ordersRoot(): DatabaseReference = fire().reference.child(ORDERS)

    fun nextOrders() = fire().reference.child(ORDERS).orderByValue()
//        .child("Qndow4Nw90dNRelvAGyMgmfAURG3")
//        .orderByChild("createdDate").equalTo(1613319828391.toDouble())

    fun orderIds() = fire().reference.child(ORDER_IDS)

    fun providerArticles(providerId: String): DatabaseReference  = fire().reference.child(ARTICLES).child(providerId)

    fun buyer(): DatabaseReference = fire().reference.child(CLIENTS).child(FireBaseAuth.getAuth().uid!!)

    fun updateArticle(id: String): DatabaseReference = fire().reference.child(ARTICLES)
        .child(FireBaseAuth.getAuth().uid!!).child(id)

    fun storage(filename: String = ""): StorageReference {
        val path = if (filename=="") "$STORAGE_PREFIX${System.currentTimeMillis()}_ttt.jpeg"
        else filename
        return FirebaseStorage.getInstance().reference.child(path)
    }

    init {
        fire().setPersistenceEnabled(true)
        orders().keepSynced(true)
        nextOrders().keepSynced(true)
        sellerProfile("").keepSynced(true)
    }
}

//76qsfkWc4rYCY36D2Eq7dnLR6743
//Qndow4Nw90dNRelvAGyMgmfAURG3
