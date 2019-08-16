package com.together.repository

import android.net.Uri

sealed class Result {

    abstract var id :String
    abstract var mode :Int

    companion object{
        val ADDED = 0
        val CHANGED = 1
        val MOVED = 2
        val REMOVED = 3
        val UNDEFINED = -1
    }

    object LoggedOut : Result(){
        override var id: String = "loggedIn"
        override var mode: Int = UNDEFINED
    }

    object LoggedIn : Result() {
        override var id: String = "loggedOut"
        override var mode: Int = UNDEFINED
    }

    data class FireDatabaseError(
        override var id: String = "ERROR FIRE DATABASE",
        override var mode: Int = UNDEFINED,
        var code: Int = Int.MAX_VALUE,
        var message: String = "",
        var detail: String = ""
    ) : Result()


    data class NewImageCreated(

        var uri: Uri?,
        override var id: String = "",
        override var mode: Int = UNDEFINED

    ) : Result()

    data class User(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
        var displayName: String = "",
        var emailAddress: String = ""
    ) : Result()


    data class Article(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
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
        override var mode: Int = UNDEFINED,
        var user: User = User(),
        var articles: List<Article> = emptyList()
    ) : Result()

    data class SellerProfile(

        override var id: String = "",
        override var mode: Int = UNDEFINED,

        var displayName: String = "",

        var firstName: String = "",
        var lastName: String = "",

        var street: String = "",
        var houseNumber: String = "",

        var city: String = "",
        var zipcode: String = "",

        var telephoneNumber: String = "",

        var lat: String = "",
        var lng: String = "",

        var markets:MutableList<Market> = mutableListOf(),

        var urls: MutableList<String> = mutableListOf(),

        var knownClientIds: MutableList<String> = mutableListOf()

        ) : Result()


    data class Market(var name: String = "",
                       var street: String = "",
                       var houseNumber: String = "",
                       var city: String = "",
                       var zipcode: String = "",
                       var dayOfWeek: String,
                       var begin: String = "",
                       var end: String = "" )



    data class ChatMessage(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
        val creatorId: String = "",
        val name: String = "",
        val text: String = "",
        val photoUrl: String = ""
    ) : Result()



}







