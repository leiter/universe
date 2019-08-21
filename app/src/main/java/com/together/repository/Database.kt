package com.together.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.together.repository.auth.FireBaseAuth

object Database {

    private fun fire() = FirebaseDatabase.getInstance().reference

    private const val ARTICLES = "articles"
    private const val PROFILE = "profile"
    private const val ORDERS = "orders"
    private const val CLIENTS = "clients"

    fun articles(): DatabaseReference = fire().child(ARTICLES).child(FireBaseAuth.getAuth()!!.uid!!)
    fun profile(): DatabaseReference = fire().child(PROFILE).child(FireBaseAuth.getAuth()!!.uid!!)
    fun orders(): DatabaseReference = fire().child(ORDERS).child(FireBaseAuth.getAuth()!!.uid!!)
    fun clients(): DatabaseReference = fire().child(CLIENTS).child(FireBaseAuth.getAuth()!!.uid!!)








}