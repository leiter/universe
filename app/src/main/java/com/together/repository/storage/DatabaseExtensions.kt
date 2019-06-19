package com.together.repository.storage

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.together.repository.Result
import io.reactivex.Observable


inline fun <reified T : Result> DatabaseReference.getObservable(): Observable<T> {

    return Observable.create { emitter ->

        val listener = addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                emitter.onError(p0.toException())
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                val i = p0.getValue(T::class.java)!!
                i.id = p0.key ?: ""
                emitter.onNext(i)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val i = p0.getValue(T::class.java)!!
                i.id = p0.key ?: ""
                emitter.onNext(i)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val i = p0.getValue(T::class.java)!!
                i.id = p0.key ?: ""
                emitter.onNext(i)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                val i = p0.getValue(T::class.java)!!
                i.id = p0.key ?: ""
                emitter.onNext(i)
            }

        })

        emitter.setCancellable { removeEventListener(listener) }

    }

}

//inline fun <reified T> DatabaseReference.getSingle(): Single<T> {
//    return Single.create { emitter ->
//        val listener = object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//                emitter.onError(p0.toException())
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                emitter.onSuccess(p0.getValue(T::class.java)!!)
//            }
//        }
//        addListenerForSingleValueEvent(listener)
//        emitter.setCancellable {
//            removeEventListener(listener)
//        }
//    }
//
//}


//inline fun <reified T> Query.getSingle(): Single<T> {
//    return Single.create { emitter ->
//
//        val valueEventListener = object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                emitter.onSuccess(p0.getValue(T::class.java)!!)
//            }
//        }
//        addListenerForSingleValueEvent(valueEventListener)
//        emitter.setCancellable { removeEventListener(valueEventListener) }
//    }
//
//}