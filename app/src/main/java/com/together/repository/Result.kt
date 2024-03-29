package com.together.repository

import com.google.firebase.auth.FirebaseUser
import java.io.File
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

        const val MODE_IMAGE_PICKER = 12
        const val MODE_CAMERA_IMAGE = 13
    }

    data class ImageLoaded(val show: Boolean) : Result() {
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

    data class NewImageCreated(
        var uri: String?,
        override var mode: Int,
        val currentImageFile: File? = null

    ) : Result(){
        override var id: String = ""
    }

    data class BuyerProfile(
        var displayName: String = "",
        var emailAddress: String = "",
        var telephoneNumber: String = "",
        var photoUrl:String = "",
        var anonymous:Boolean = true,
        var defaultMarket: String = "",
        var defaultPickUpTime: String = "",
//        var userDevices: List<Device> = emptyList(),
        var placedOrderIds: Map<String,String> = emptyMap(), // Date , orderId
        ) : Result() {

//        @get:Exclude @set:Exclude
        override var id: String = ""
//        @get:Exclude @set:Exclude
        override var mode: Int = UNDEFINED
    }

    data class Article(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
        var productId: String = "",
        var productName: String = "",
        var available: Boolean = false,
        var unit: String = "",
        var price: Double = 0.0,
        var weighPerPiece: Double = 0.0,
        var imageUrl: String = "",
        var category: String = "",
        var searchTerms: String = "",
        var detailInfo: String = "",
//         @get:PropertyName("adsfg")
//        val fuckck:String =  "Fuck"
    ) : Result()

    data class OrderedProduct(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
        var productId: String = "-1",
        var productName: String = "",
        var unit: String = "",
        var price: Double = 0.0,
        var amount: String = "",
        var amountCount: Double = 0.0,
        var piecesCount: Int = -1,
        ): Result()

    data class Order(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
        var buyerProfile: BuyerProfile = BuyerProfile(),
        var createdDate: Long = 0L,
        var sellerId: String = "",
        var marketId: String = "",
        var pickUpDate: Long = 0L,
        var message: String = "",
        var notFavourite: Boolean = true,
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
        var zipCode: String = "",
        var telephoneNumber: String = "",

        var lat: String = "",
        var lng: String = "",
        var sellerId: String = "",

        var markets: MutableList<Market> = mutableListOf(),
        var urls: MutableList<String> = mutableListOf(),
        var knownClientIds: MutableList<String> = mutableListOf(),
//        var employeeIds: MutableList<Employee> = mutableListOf(),

    ) : Result()

    data class Market(
        var name: String = "",
        var street: String = "",
        var houseNumber: String = "",
        var city: String = "",
        var zipCode: String = "",
        var dayOfWeek: String = "",
        var begin: String = "",
        var end: String = "",
        var dayIndex: Int = UNDEFINED,
        override var id: String = UUID.randomUUID().toString()
    ) : Result() {
        override var mode: Int = UNDEFINED
    }

    data class SetDetailDescription(val text: String) : Result(){
        override var id: String = ""
        override var mode: Int = UNDEFINED
    }

    data class CleanUpResult(
        val started: Boolean = false,
        val placedOrderIds: Map<String, String> = emptyMap(),
        val profileDeleted: Boolean = false,
        val deletedOrders: List<String> = emptyList()
    ): Result() {
        override var id: String = ""
        override var mode: Int = UNDEFINED
    }
}

fun Result.Order.changed(otherOrder: Result.Order): Boolean {
    return when {
        this.articles.size != otherOrder.articles.size -> true
        this.pickUpDate != otherOrder.pickUpDate -> true
        this.message != otherOrder.message -> true
//        this.articles.changed(otherOrder.articles) -> true
        else -> false
    }
}

fun List<Result.OrderedProduct>.changed(otherList: List<Result.OrderedProduct>): Boolean {
    val allIds = otherList.map { it.id }
    return when {
        this.filter { allIds.contains(it.id) }.size != this.size -> true
        this.any { it.amountCount != otherList.filter {
                otherIt -> it.id == otherIt.id }[0].amountCount
        } -> true

        else -> false
    }
}