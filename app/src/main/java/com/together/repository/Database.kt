package com.together.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.together.repository.auth.FirebaseAuth

object Database {

    private fun fire() = FirebaseDatabase.getInstance().reference

    private const val ARTICLES = "articles"
    private const val PROFILE = "profile"
    private const val ORDERS = "orders"
    private const val CLIENTS = "clients"

    fun articles(): DatabaseReference = fire().child(FirebaseAuth.fireUser!!.uid).child(ARTICLES)
    fun profile(): DatabaseReference = fire().child(FirebaseAuth.fireUser!!.uid).child(PROFILE)
    fun orders(): DatabaseReference = fire().child(FirebaseAuth.fireUser!!.uid).child(ORDERS)
    fun clients(): DatabaseReference = fire().child(FirebaseAuth.fireUser!!.uid).child(CLIENTS)






}