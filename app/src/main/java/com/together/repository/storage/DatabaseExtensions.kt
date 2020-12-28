package com.together.repository.storage

import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.together.repository.Result
import com.together.repository.Result.Companion.ADDED
import com.together.repository.Result.Companion.CHANGED
import com.together.repository.Result.Companion.MOVED
import com.together.repository.Result.Companion.REMOVED
import io.reactivex.Observable
import io.reactivex.Single


inline fun <reified T : Result> DatabaseReference.getObservable(): Observable<T> {

    return Observable.create { emitter ->

        val listener = addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                emitter.onError(p0.toException())
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                val i = p0.getValue(T::class.java)!!
                i.id = p0.key ?: ""
                i.mode = MOVED
                emitter.onNext(i)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val i = p0.getValue(T::class.java)!!
                i.id = p0.key ?: ""
                i.mode = CHANGED
                emitter.onNext(i)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val i = p0.getValue(T::class.java)!!
                i.id = p0.key ?: ""
                i.mode = ADDED
                emitter.onNext(i)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                val i = p0.getValue(T::class.java)!!
                i.id = p0.key ?: ""
                i.mode = REMOVED
                emitter.onNext(i)
            }

        })

        emitter.setCancellable { removeEventListener(listener) }

    }

}

fun DatabaseReference.getSingle(child: String): Single<Boolean> {
    return Single.create { emitter ->
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                emitter.onError(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                val i = p0.hasChild(child)
                emitter.onSuccess(i)
            }
        }
        addListenerForSingleValueEvent(listener)
        emitter.setCancellable {
            removeEventListener(listener)
        }
    }

}

fun DatabaseReference.getSingleExists(): Single<Boolean> {
    return Single.create { emitter ->
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                emitter.onError(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                val i = p0.exists()
                emitter.onSuccess(i)
            }
        }

        addListenerForSingleValueEvent(listener)
        emitter.setCancellable {
            removeEventListener(listener)
        }
    }

}


inline fun <reified T> Query.getSingle(): Single<T> {
    return Single.create { emitter ->

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                emitter.onError(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                emitter.onSuccess(p0.getValue(T::class.java)!!)
            }
        }
        addListenerForSingleValueEvent(valueEventListener)
        emitter.setCancellable { removeEventListener(valueEventListener) }
    }

}

fun Task<Void>.getCompletable(): Single<Boolean> {
    return Single.create { emitter ->
        addOnCompleteListener { emitter.onSuccess(true) }
        addOnCanceledListener { emitter.onSuccess(false) }
        addOnFailureListener { emitter.onError(it) }
    }

}

