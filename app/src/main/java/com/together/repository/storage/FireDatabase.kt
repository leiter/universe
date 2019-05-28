package com.together.repository.storage

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.together.app.MainMessagePipe
import com.together.repository.Result

class FireDatabase {

    //Create Enum
    fun createChildEventListener(
        enum: DatabaseManager,
        ref: DatabaseReference
    ) {

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
        MainMessagePipe.listenerMap.put(connection,Pair(connection,ref))
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

    fun createDocument(ref: DatabaseReference, path: String, document: Result){
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
            MainMessagePipe.mainThreadMessage.onNext(result
//                DatabaseManager.ARTICLE_LIST.getValueClazz().newInstance()
            )
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

    }
}

fun createChildEventListener(
    enum: DatabaseManager
):
        ChildEventListener {

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
                enum.getValueClazz().newInstance()
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

    return connection
}

enum class DatabaseManager {


    ARTICLE_LIST {

        lateinit var connection :ChildEventListener

        override fun setup(
            enum: DatabaseManager,
            ref: DatabaseReference,
            bag: MutableMap<String, Pair<ChildEventListener, DatabaseReference>>
        ) {
            val articles = ref.child("articles")

            connection = articles.addChildEventListener(createChildEventListener(ARTICLE_LIST))
            bag[this.name] = Pair(connection,articles)

        }


        override fun getValueClazz(): Class<Result.Article> {
            return Result.Article::class.java
        }


    },

    LAST_ACTIVE_CHATS {
        override fun setup(
            enum: DatabaseManager,
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
        enum: DatabaseManager,
        ref: DatabaseReference,
        bag: MutableMap<String, Pair<ChildEventListener, DatabaseReference>>
    )


}