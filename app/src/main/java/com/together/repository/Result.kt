package com.together.repository

import android.net.Uri


// Results are used for Requests and Results this will probably change in future
sealed class Result(var callbackType : Int = -1) {

    abstract var id :String

    companion object{
        val CHILD_ADDED = 0
        val CHILD_CHANGED = 1
        val CHILD_MOVED = 2
        val CHILD_REMOVED = 3
    }


    object LoggedOut : Result(){
        override var id: String = "loggedIn"
    }

    object LoggedIn : Result() {
        override var id: String = "loggedOut"

    }

    data class FireDatabaseError(
        override var id: String = "ERROR FIRE DATABASE",
        var code: Int = Int.MAX_VALUE,
        var message: String = "",
        var detail: String = ""
    ) : Result()


    data class NewImageCreated(override var id: String = "", var uri: Uri?) : Result()

    data class User(
        override var id: String = "",
        var displayName: String = "",
        var emailAdress: String = ""
    ) : Result()


    data class Article(
        override var id: String = "",
        var productId: Int = -1,
        var productName: String = "",
        var productDescription: String? = null,
        var available: Boolean = false,
        var unit: String = "",
        var pricePerUnit: String = "",
        var imageUrl: String = ""
    ) : Result()


    data class Order(
        override var id: String = "",
        var user: User = User(),
        var articles: List<Article> = emptyList()
    ) : Result()

    data class ChatMessage(
        override var id: String = "",
        val creatorId: String = "",
        val name: String = "",
        val text: String = "",
        val photoUrl: String = ""
    ) : Result()



}








