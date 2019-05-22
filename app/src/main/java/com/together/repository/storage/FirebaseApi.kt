package com.together.repository.storage

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.together.app.*
import java.util.*
import javax.inject.Inject




class Firebase : FirebaseAuth.AuthStateListener {

//    private val listenerMap: MutableMap<FireDataBaseType, Pair<DatabaseReference, ChildEventListener>> = hashMapOf()

    @Inject
    lateinit var database : FirebaseDatabase


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
                LoggedOut(hashCode(), Date(), Date())
            )
        } else {
            MainMessagePipe.mainThreadMessage.onNext(
                LoggedIn(hashCode(), Date(), Date())
            )
        }
    }

    fun getSupplyList(ref: DatabaseReference) {
        Log.e("Enter", "For debugging");

        ref.child("articles").addChildEventListener(listener)


    }

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


}