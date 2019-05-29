package com.together.repository.storage

import com.google.firebase.database.*
import com.together.app.MainMessagePipe
import com.together.repository.Result

class FireDatabase {

    //Create Enum
    fun createChildEventListener(
        enum: DatabaseManager,
        ref: DatabaseReference
    ): ChildEventListener {

        val connection = object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {
                MainMessagePipe.mainThreadMessage.onNext(
                    Result.FireDatabaseError(
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
                MainMessagePipe.mainThreadMessage.onNext(
                    enum.getValueClazz().newInstance()
                )
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                MainMessagePipe.mainThreadMessage.onNext(
                    enum.getValueClazz().newInstance()
                )
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                MainMessagePipe.mainThreadMessage.onNext(
                    enum.getValueClazz().newInstance()
                )
            }

        }

//        return Pair(connection, ref)
        MainMessagePipe.listenerMap.put(connection, Pair(connection, ref))
        return connection
    }

    fun getSupplyList(ref: DatabaseReference) {
        ref.child("articles").addChildEventListener(listener)
    }

    fun createArticle(ref: DatabaseReference, article: Result) {
        ref.child("articles").push().setValue(article)
            .addOnFailureListener {
                MainMessagePipe.mainThreadMessage.onError(it)
            }
            .addOnCompleteListener {
                MainMessagePipe.mainThreadMessage.onNext(article)
            }
    }


    fun loadAvailableArticles(ref: DatabaseReference, path: String, document: Result) {
        ref.database.getReference("article")
    }


    fun createDocument(ref: DatabaseReference, path: String, document: Result) {
        ref.child(path).push().setValue(document)
    }

    val listener: ChildEventListener = object : ChildEventListener {

        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val result = p0.getValue(Result.Article::class.java)
                    as Result.Article   //find a solution that does not use assertion and no cast
            MainMessagePipe.mainThreadMessage.onNext(result)
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

    }
}

fun createChildEventListener(enum: DatabaseManager): ChildEventListener {

    val connection = object : ChildEventListener {

        override fun onCancelled(p0: DatabaseError) {
            MainMessagePipe.mainThreadMessage.onNext(
                Result.FireDatabaseError(
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
            MainMessagePipe.mainThreadMessage.onNext(
                p0.getValue(enum.getValueClazz())!!
            )
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

fun createValueListener(
    enum: DatabaseManager
) : ValueEventListener {

   return object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            MainMessagePipe.mainThreadMessage.onNext(
                Result.FireDatabaseError(p0.code, p0.message, p0.details)
            )
        }

        override fun onDataChange(p0: DataSnapshot) {
            MainMessagePipe.mainThreadMessage.onNext(
                p0.getValue(enum.getValueClazz())!!
            )
        }
    }
}


enum class DatabaseManager {

    ARTICLE_LIST {

        override fun setup(
            ref: DatabaseReference,
            bag: MutableMap<String, Pair<ChildEventListener, DatabaseReference>>
        ) {
            val articles = ref.child("articles")

            val connection: ChildEventListener
            connection = articles.addChildEventListener(createChildEventListener(ARTICLE_LIST))

            bag[articles.key!!] = Pair(connection, articles)  // todo test

        }


        override fun getValueClazz(): Class<Result.Article> {
            return Result.Article::class.java
        }


    },

    LOAD_ARTICLES {
        override fun getValueClazz(): Class<Result.ArticleList> = Result.ArticleList::class.java

        override fun setup(
            ref: DatabaseReference,
            bag: MutableMap<String, Pair<ChildEventListener, DatabaseReference>>
        ) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


    },

    LAST_ACTIVE_CHATS {
        override fun setup(
            ref: DatabaseReference,
            bag: MutableMap<String, Pair<ChildEventListener,
                    DatabaseReference>>
        ) {

        }

        override fun getValueClazz(): Class<Result.ChatThread> {
            return Result.ChatThread::class.java
        }

    };


    abstract fun getValueClazz(): Class<out Result>

    abstract fun setup(
        ref: DatabaseReference,
        bag: MutableMap<String, Pair<ChildEventListener, DatabaseReference>>
    )


}