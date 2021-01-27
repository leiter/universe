package com.together.base

import android.net.Uri
import android.os.Parcelable
import com.together.repository.Result
import kotlinx.android.parcel.Parcelize

sealed class UiState {

    abstract var _id: String
    abstract var _mode: Int

    companion object {
        const val ADDED = 0
        const val CHANGED = 1
        const val MOVED = 2
        const val REMOVED = 3
        const val UNDEFINED = -1
    }

    data class LoadingProgress(val progressViewId: Int, val show: Boolean)



    object LOGGEDOUT : UiState() {
        override var _id = "StateLoggedOut"
        override var _mode: Int = UNDEFINED
    }

    object LOGIN_REQUIRED : UiState() {
        override var _id = "StateLoggedOut"
        override var _mode: Int = UNDEFINED
    }

    object ACCOUNT_DELETED : UiState() {
        override var _id = "StateLoggedOut"
        override var _mode: Int = UNDEFINED
    }

    data class BaseAuth(val buyerProfile: BuyerProfile) : UiState() {
        override var _id = "StateLoggedIn"
        override var _mode: Int = UNDEFINED
    }

    data class OrderedProduct(
        override var _id: String = "",
        override var _mode: Int = UNDEFINED,
        var productId: Int = -1,
        var productName: String = "",
        var unit: String = "",
        var price: Double = 0.0,
        var amount: String = "",
        var piecesCount: Int = -1
    ): UiState()

    data class Article(
        var productId: Int = -1,
        var productName: String = "",
        var productDescription: String? = null,
        var available: Boolean = false,
        var remoteImageUrl: String = "",
        var isSelected: Boolean = false,
        var amount: String = "",
        var pricePerUnit: String = "",
        var unit: String = "",
        var amountCount: Double = 0.0,
        var priceDigit: Double = 0.0,
        var category: String = "",
        var detailInfo: String = "",
        var searchTerms: String = "",
        var weightPerPiece: Double = 0.0,
        var pieces: Int = 0,
        override var _id: String = "",
        override var _mode: Int = UNDEFINED

    ) : UiState() {
        var pieceCounter: Int = 0
            set(value) {
                field = value
                pieces = value
                amountCount = value.toDouble() * weightPerPiece
            }
        var amountDisplay: String = prepareAmountDisplay()
        val priceDisplay: String
            get() {
                return "%.2f€".format((priceDigit * amountCount)).replace(".", ","); }

        private fun prepareAmountDisplay(): String {
            val amountStart: String = if (unit != "kg") {
                amountCount.toString().split(".")[0]
            } else "%.3f".format(amountCount).replace(".", ",")
            return "$amountStart $unit"
        }

        fun calculateAmountCountDisplay(): String {
            return if (unit != "kg") amountCount.toString().split(".")[0]
            else "%.3f".format(amountCount).replace(".", ",")
        }

        fun getWeightText(): String {
            return if (unit != "kg") "" else {
                val result: String
                result = when {
                    weightPerPiece >= 1 -> {
                        "Ca. ${this.weightPerPiece}kg pro Stück."
                    }
                    else -> {
                        val s = (this.weightPerPiece * 1000).toString().split(".")[0]
                        "Ca. ${s}g pro Stück."
                    }
                }
                result
            }
        }
    }

    data class NewProductImage(val uri: Uri)

    data class Order(
        override var _id: String = "",
        override var _mode: Int = Result.UNDEFINED,
        var buyerProfile: BuyerProfile = BuyerProfile(),
        var createdDate: Long = 0L,
        var marketId: String = "",
        var pickUpDate: Long = 0L,
        var message: String = "",
        var productList: List<OrderedProduct> = emptyList(),
        ) : UiState()



    data class BuyerProfile(

        override var _id: String = "",
        override var _mode: Int = Result.UNDEFINED,

        var displayName: String = "",

        var emailAddress: String = "",

        var contactIds: MutableList<String> = mutableListOf(),

        var sellerIds: MutableList<String> = mutableListOf(),

        var providerId: String =  "",
        var isAnonymous: Boolean = true,
        var photoUrl: String = "",
        var phoneNumber: String = "",
        var defaultMarket: String = "",
        var defaultTime: String  = ""

    ) : UiState()

    data class SellerProfile(
        override var _id: String = "",
        var displayName: String = "",
        var firstName: String = "",
        var lastName: String = "",
        var street: String = "",
        var houseNumber: String = "",
        var city: String = "",
        var zipCode: String = "",
        var _telephoneNumber: String = "",

        var _lat: String = "",
        var _lng: String = "",

        var marketList: MutableList<Market> = mutableListOf(),
        var _urls: MutableList<String> = mutableListOf(),
        var _knownClientIds: MutableList<String> = mutableListOf(),

        ) : UiState() {
        override var _mode: Int = UNDEFINED
    }


    @Parcelize
    data class Market(
        var name: String = "",
        var street: String = "",
        var houseNumber: String = "",
        var city: String = "",
        var zipCode: String = "",
        var dayOfWeek: String,
        var begin: String = "",
        var end: String = "",
        var dayIndicator: Int = UNDEFINED,
        override var _id: String = "",
        override var _mode: Int = UNDEFINED
    ) : UiState(), Parcelable


}