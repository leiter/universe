package com.together.repository.storage

import com.google.firebase.database.DatabaseReference
import com.together.app.MainMessagePipe
import com.together.repository.Result

enum class DatabaseManager {

    ARTICLE_LIST {
        override fun setup(ref: DatabaseReference) {
            val articles = ref.child("articles")
            val connection = articles.addChildEventListener(createChildEventListener(ARTICLE_LIST))
            MainMessagePipe.listenerMap[connection] = Pair(connection, articles)
        }

        override fun getValueClazz(): Class<Result.Article> = Result.Article::class.java

    },

    LOAD_ARTICLES {

        override fun getValueClazz(): Class<Result.ArticleList> = Result.ArticleList::class.java


        override fun setup(ref: DatabaseReference) {


        }


    },

    LAST_ACTIVE_CHATS {


        override fun setup(ref: DatabaseReference) {

        }

        override fun getValueClazz(): Class<Result.ChatThread> = Result.ChatThread::class.java


    };








    abstract fun getValueClazz(): Class<out Result>

    abstract fun setup(ref: DatabaseReference)


}