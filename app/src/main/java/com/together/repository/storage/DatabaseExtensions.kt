package com.together.repository.storage

import com.google.firebase.database.*
import io.reactivex.Observable
import io.reactivex.Single


inline fun <reified T> Query.getSingle(): Single<T> {
    return Single.create { emitter ->

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                emitter.onSuccess(p0.getValue(T::class.java)!!)
            }
        }
        addListenerForSingleValueEvent(valueEventListener)
        emitter.setCancellable { removeEventListener(valueEventListener) }
    }

}


inline fun <reified T> DatabaseReference.getObservable(): Observable<T> {

    return Observable.create { emitter ->

        val listener = addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                emitter.onError(p0.toException())
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                emitter.onNext(p0.getValue(T::class.java)!!)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                emitter.onNext(p0.getValue(T::class.java)!!)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                emitter.onNext(p0.getValue(T::class.java)!!)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                emitter.onNext(p0.getValue(T::class.java)!!)
            }

        })

        emitter.setCancellable { removeEventListener(listener) }

    }

}

inline fun <reified T> DatabaseReference.getSingle(): Single<T> {
    return Single.create { emitter ->
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                emitter.onError(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                emitter.onSuccess(p0.getValue(T::class.java)!!)
            }
        }
        addListenerForSingleValueEvent(listener)
        emitter.setCancellable {
            removeEventListener(listener)
        }
    }

}
