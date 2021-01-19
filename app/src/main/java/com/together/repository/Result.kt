package com.together.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import java.util.*

sealed class Result {

    abstract var id: String
    abstract var mode: Int

    companion object {
        const val ADDED = 0
        const val CHANGED = 1
        const val MOVED = 2
        const val REMOVED = 3
        const val UNDEFINED = -1
    }

    data class ImageLoaded(val progressId: Int, val show: Boolean) : Result() {
        override var id: String = "No result"
        override var mode: Int = UNDEFINED
    }

    object Empty : Result() {
        override var id: String = "No result"
        override var mode: Int = UNDEFINED
    }

    object LoggedOut : Result() {
        override var id: String = "loggedIn"
        override var mode: Int = UNDEFINED
    }

    data class LoggedIn(val currentUser: FirebaseUser) : Result() {
        override var id: String = "loggedOut"
        override var mode: Int = UNDEFINED
    }

    object AccountDeleted : Result() {
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
        var displayName: String = "",
        var emailAddress: String = "",
        var telephoneNumber: String = "",
        var photoUrl:String = "",
        ) : Result() {
        override var id: String = ""
        override var mode: Int = UNDEFINED

    }

    data class Article(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
        var productId: Int = -1,
        var productName: String = "",
        var available: Boolean = false,
        var unit: String = "",
        var price: Double = 0.0,
        var weighPerPiece: Double = 0.0,
        var imageUrl: String = "",
        var category: String = "",
        var searchTerms: String = "",
        var detailInfo: String = ""
//         @get:PropertyName("adsfg")
//        val fuckck:String =  "Fuck"
    ) : Result()

    data class OrderedProduct(
        override var id: String,
        override var mode: Int = UNDEFINED,
        var productId: Int = -1,
        var productName: String = "",
        var unit: String = "",
        var price: Double = 0.0,
        var amount: String,
        var piecesCount: Int = -1


        ): Result()

    data class Order(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
        var user: User = User(),
        var createdDate: Long = 0L,
        var marketId: String = "",
        var dueDate: Long = 0L,
        var articles: List<OrderedProduct> = emptyList(),
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
        var sellerId: String = "",

        var markets: MutableList<Market> = mutableListOf(),
        var urls: MutableList<String> = mutableListOf(),
        var knownClientIds: MutableList<String> = mutableListOf()

    ) : Result()

    data class BuyerProfile(

        override var id: String = "",
        override var mode: Int = UNDEFINED,

        var displayName: String = "",

        var emailAddress: String = "",

        var contactIds: MutableList<String> = mutableListOf(), // int is to order 10 100 1000
        var sellerIds: MutableList<String> = mutableListOf() // int is to order 10 100 1000

    ) : Result()


    data class Market(
        var name: String = "",
        var street: String = "",
        var houseNumber: String = "",
        var city: String = "",
        var zipcode: String = "",
        var dayOfWeek: String = "",
        var begin: String = "",
        var end: String = "",
        var dayIndex: Int = UNDEFINED
    ) : Result() {
        override var id: String = UUID.randomUUID().toString()
        override var mode: Int = UNDEFINED

    }

}

