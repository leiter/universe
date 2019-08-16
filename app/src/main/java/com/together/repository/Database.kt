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

    fun articles(): DatabaseReference = fire().child(ARTICLES).child(FirebaseAuth.fireUser!!.uid)
    fun profile(): DatabaseReference = fire().child(PROFILE).child(FirebaseAuth.fireUser!!.uid)
    fun orders(): DatabaseReference = fire().child(ORDERS).child(FirebaseAuth.fireUser!!.uid)
    fun clients(): DatabaseReference = fire().child(CLIENTS).child(FirebaseAuth.fireUser!!.uid)







}