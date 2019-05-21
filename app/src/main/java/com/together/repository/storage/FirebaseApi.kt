package com.together.repository.storage

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.together.app.Article
import com.together.app.LoggedState
import com.together.app.MainMessagePipe


sealed class FireBaseMessage {
    class LoggedOut : FireBaseMessage()
    class LoggedIn : FireBaseMessage()
    data class Supply(val articles: List<Article>) : FireBaseMessage()


}


class Firebase : FirebaseAuth.AuthStateListener {

//    private val listenerMap: MutableMap<FireDataBaseType, Pair<DatabaseReference, ChildEventListener>> = hashMapOf()

    //Create Enum
    fun <T> getChildEventListener(clazz: Class<T>): ChildEventListener {

        return object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        }
    }

    fun isLoggedIn(): LoggedState {
        when (firebaseAuth.currentUser != null) {
            true -> return LoggedState.IN()
            else -> return LoggedState.OUT()
        }
    }


    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    init {
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        val user = p0.currentUser
        if (user == null) {
            MainMessagePipe.mainThreadMessage.onNext(
                FireBaseMessage.LoggedOut()
            )
        } else {
            MainMessagePipe.mainThreadMessage.onNext(
                FireBaseMessage.LoggedIn()
            )
        }
    }

    fun getSupplyList() {
        Log.e("Enter", "For debugging");

        reference.addChildEventListener(listener)


    }

    val reference = firebaseDatabase.getReference().child("articles")

    val listener: ChildEventListener = object : ChildEventListener {

        override fun onCancelled(p0: DatabaseError) {
            Log.e("TTTTT", "For debugging");

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            Log.e("TTTTT", "For debugging");

        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            Log.e("TTTTT", "For debugging");

        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val result = p0.getValue(Da::class.java)!!
            MainMessagePipe.mainThreadMessage.onNext(result)  //Solution not use assertation
            Log.e("TTTTT", "Adddded");

        }

        override fun onChildRemoved(p0: DataSnapshot) {
            Log.e("TTTTT", "For debugging");

        }

    }

    @IgnoreExtraProperties
    data class Da(var available:Boolean, var name:String, var views:String)

}