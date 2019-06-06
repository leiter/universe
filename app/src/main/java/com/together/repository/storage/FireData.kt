package com.together.repository.storage

import com.google.firebase.database.*
import com.together.app.MainMessagePipe
import com.together.repository.Result

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


    fun loadAvailableArticles(ref: DatabaseReference, path: String, document: Result) {
        ref.database.getReference(path).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {

                }
                ref.database.getReference("article").removeEventListener(this)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }


    fun createDocument(ref: DatabaseReference, path: String, document: Result) {
        ref.child(path).push().setValue(document)
    }

}

fun createChildEventListener(enum: DatabaseManager): ChildEventListener {

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
            MainMessagePipe.mainThreadMessage.onNext(
                p0.getValue(enum.getValueClazz())!!
            )
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            val i = p0.getValue(enum.getValueClazz())!!
            MainMessagePipe.mainThreadMessage.onNext(i)
        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            MainMessagePipe.mainThreadMessage.onNext(
                p0.getValue(enum.getValueClazz())!!
            )
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            MainMessagePipe.mainThreadMessage.onNext(
                p0.getValue(enum.getValueClazz())!!
            )
        }
    }
    return connection
}

fun createValueListener(enum: DatabaseManager): ValueEventListener {

    return object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            MainMessagePipe.mainThreadMessage.onNext(
                Result.FireDatabaseError("", p0.code, p0.message, p0.details))
        }

        override fun onDataChange(p0: DataSnapshot) {

            for (p in p0.children){
                MainMessagePipe.mainThreadMessage.onNext(
                    p.getValue(enum.getValueClazz())!!
                )
            }

        }
    }
}


