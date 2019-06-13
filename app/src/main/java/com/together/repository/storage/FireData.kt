package com.together.repository.storage

import com.google.firebase.database.*
import com.together.app.MainMessagePipe
import com.together.repository.Result
import io.reactivex.Observable
import io.reactivex.Single

class FireData {

    fun createArticle(ref: DatabaseReference, article: Result) {
        val d = ref.child("articles").push().setValue(article)
            .addOnFailureListener {
                MainMessagePipe.mainThreadMessage.onError(it)
            }
            .addOnCompleteListener {
                MainMessagePipe.mainThreadMessage.onNext(article)
            }
    }


    fun createDocument(ref: DatabaseReference, path: String, document: Result) {
        ref.child(path).push().setValue(document)
    }

}

fun createChildEventListener(enum: Class<out Result>): ChildEventListener {

    val connection = object : ChildEventListener {

        override fun onCancelled(p0: DatabaseError) {
            MainMessagePipe.mainThreadMessage.onNext(
                Result.FireDatabaseError(
                    "",
                    p0.code,
                    p0.message,
                    p0.details
                )
            )
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//            MainMessagePipe.mainThreadMessage.onNext(
//                p0.getValue(enum.getValueClazz())!!
//            )
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//            val i = p0.getValue(enum.getValueClazz())!!
//            i.id = p0.key ?: ""
//            MainMessagePipe.mainThreadMessage.onNext(i)
        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            MainMessagePipe.mainThreadMessage.onNext(
                p0.getValue(enum)!!
            )
        }

        override fun onChildRemoved(p0: DataSnapshot) {
//            MainMessagePipe.mainThreadMessage.onNext(
//                p0.getValue(enum.getValueClazz())!!
//            )
        }
    }
    return connection
}


inline fun <reified T> Query.getObservable(): Single<T> {
    return Single.create { emitter ->

        val valueEventListener = object :  ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {

                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }


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
        emitter.setCancellable { removeEventListener(listener) }
    }

}