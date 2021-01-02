package com.together.base

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.NumberFormat

sealed class UiState {

    abstract var _id: String
    abstract var _mode: Int

    companion object {

        const val ADDED = 0
        const val CHANGED = 1
        const val MOVED = 2
        const val REMOVED = 3
        const val UNDEFINED = -1

        const val UNIT_UNDEFINED = -1

    }

    object LOGGEDOUT : UiState() {
        override var _id = "StateLoggedOut"
        override var _mode: Int = MOVED
    }

    object ACCOUNT_DELETED : UiState() {
        override var _id = "StateLoggedOut"
        override var _mode: Int = MOVED
    }

    object BASE_AUTH : UiState() {
        override var _id = "StateLoggedIn"
        override var _mode: Int = MOVED
    }

    data class Article(
        override var _id: String = "",
        var productId: Int = -1,
        var productName: String = "",
        var productDescription: String? = null,
        var available: Boolean = false,
        var remoteImageUrl: String = "",
        var localImageUrl: String = "",
        var amount: String = "",
        var pricePerUnit: String = "",
        var unit: String = "",
        var amountCount: Double = 0.0,
        var priceDigit: Double = 0.0,

        override var _mode: Int = MOVED

    ) : UiState(){
        var amountDisplay: String = prepareAmountDisplay()
        var amountCountDisplay: String = if(unit!="kg") amountCount.toString().split(".")[0]
                                        else amountCount.toString()
        var priceDisplay: String = "0,00€"
        get() { return "%.2f€".format((priceDigit*amountCount)); }
        private fun prepareAmountDisplay(): String {
            val amountStart: String
            if (unit!="kg"){
                amountStart = amountCount.toString().split(".")[0]
            } else amountStart = amountCount.toString().replace(".",",")
            return "$amountStart $unit"
        }
        fun calculateAmountCountDisplay(): String {
            return if(unit!="kg") amountCount.toString().split(".")[0]
            else amountCount.toString()
        }
    }

    data class NewProductImage(val uri: Uri)

    data class SellerProfile(

        override var _id: String = "",

        var displayName: String = "",

        var firstName: String = "",
        var lastName: String = "",
        var street: String = "",
        var houseNumber: String = "",

        var city: String = "",
        var zipcode: String = "",

        var _telephoneNumber: String = "",

        var _lat: String = "",
        var _lng: String = "",

        var _urls: MutableList<String> = mutableListOf(),

        var _knownClientIds: MutableList<String> = mutableListOf(),
        override var _mode: Int = MOVED

    ) : UiState()


    @Parcelize
    data class Market(var name: String = "",
                      var street: String = "",
                      var houseNumber: String = "",
                      var city: String = "",
                      var zipcode: String = "",
                      var dayOfWeek: String,
                      var begin: String = "",
                      var end: String = "",
                      override var _id: String = "",
                      override var _mode: Int = MOVED
    ) : UiState(), Parcelable


    data class ChatMessage(
        override var _id: String = "",
        var creatorId: String = "",
        var name: String = "",
        var text: String = "",
        var photoUrl: String = "",
        override var _mode: Int = MOVED

    ) : UiState()

    data class PostAnyMessage(
        override var _id: String = "", var creatorId: String,
        var name: String = "",
        var text: String = "",
        var photoUrl: String = "",
        override var _mode: Int = MOVED

    ) : UiState()


    data class Unit(
        var name: String = "",
        var price: String = "",
        var averageWeight: String = "",
        override var _id: String = "",
        override var _mode: Int = UNIT_UNDEFINED
    ) : UiState()
}