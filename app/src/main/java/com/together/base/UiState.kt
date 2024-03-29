package com.together.base

import android.net.Uri
import android.os.Parcelable
import com.together.repository.Result
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

sealed class UiState {

    abstract var id: String
    abstract var mode: Int

    companion object {
        const val ADDED = 0
        const val CHANGED = 1

        //        const val MOVED = 2
        const val REMOVED = 3
        const val UNDEFINED = -1
    }

    data class LoadingProgress(val show: Boolean)

    object LoggedOut : UiState() {
        override var id = "StateLoggedOut"
        override var mode: Int = UNDEFINED
    }

    data class LoginRequired(
        override var id: String = "StateLoggedOut",
        override var mode: Int = UNDEFINED
    ) : UiState()

    object AccountDeleted : UiState() {
        override var id = "StateLoggedOut"
        override var mode: Int = UNDEFINED
    }

    data class BaseAuth(
        override var id: String = "StateLoggedIn",
        override var mode: Int = UNDEFINED,
        var hasProfile: Boolean = false
    ) : UiState()

    data class OrderedProduct(
        override var id: String = "",
        override var mode: Int = UNDEFINED,
        var productId: String = "",
        var productName: String = "",
        var unit: String = "",
        var price: Double = 0.0,
        var amount: String = "",
        val amountCount: Double = 0.0,
        var piecesCount: Int = -1
    ) : UiState()

    data class Article(
        var productId: String = "",
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
        var inBasket: Boolean = false,
        override var id: String = "",
        override var mode: Int = UNDEFINED

    ) : UiState() {

        fun canBeCounted() : Boolean {
            return (unit.toLowerCase(Locale.ROOT) == "kg" && weightPerPiece == 0.0)
        }

        fun prepareSearchTerms(): String {
            val bag = "$searchTerms,$category,$productName".replace(","," ")
                .trim().replace("""\s{2,}""".toRegex(), " ")
                .replace(" ",",").split(",").toSet()
            return bag.joinToString(",")
        }
        var pieceCounter: Int = 0
            set(value) {
                field = value
                pieces = value
                if (pieceCounter > 0) {
                    amountCount = value.toDouble() * weightPerPiece
                }
            }
        var amountDisplay: String = prepareAmountDisplay()
        val priceDisplay: String
            get() {
                return "%.2f€".format((priceDigit * amountCount)).replace(".", ","); }

        private fun prepareAmountDisplay(): String {
            val amountStart: String = if (unit.toLowerCase(Locale.ROOT) != "kg") {
                amountCount.toString().split(".")[0]
            } else "%.3f".format(amountCount).replace(".", ",")
            return "$amountStart $unit"
        }

        fun calculateAmountCountDisplay(): String {
            return if (unit.toLowerCase(Locale.ROOT) != "kg") amountCount.toString().split(".")[0]
            else "%.3f".format(amountCount).replace(".", ",")
        }

        fun getWeightText(): String {
            return if (unit.toLowerCase(Locale.ROOT) != "kg") unit else {
                val result: String = when {
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

    data class NewProductImage(val uri: Uri, val origin: Int, val currentImageFile: File? = null)

    data class Order(
        override var id: String = "",
        override var mode: Int = Result.UNDEFINED,
        var buyerProfile: BuyerProfile = BuyerProfile(),
        var createdDate: Long = 0L,
        var marketId: String = "",
        var pickUpDate: Long = 0L,
        var message: String = "",
        var isNotFavourite: Boolean = true,
        var productList: List<OrderedProduct> = emptyList(),
    ) : UiState() {
        fun createProductList(): String {
            var result = ""
            productList.forEachIndexed { index, it ->
                result += if (index != productList.size - 1) it.productName + ", " else it.productName
            }
            return result
        }

        fun createProductCount(): String {
            return "${productList.size} Produkte"
        }
    }

    data class BuyerProfile(
        var displayName: String = "",

        var emailAddress: String = "",

        var contactIds: MutableList<String> = mutableListOf(),
        var sellerIds: MutableList<String> = mutableListOf(),

        var providerId: String = "",
        var isAnonymous: Boolean = true,
        var photoUrl: String = "",
        var phoneNumber: String = "",
        var defaultMarket: String = "",
        var defaultTime: String = "",
        var placedOrderIds: Map<String, String> = emptyMap(),

        ) : UiState() {
        fun getDefaultTimeDisplay(): String {
            return "$defaultTime Uhr"
        }

        override var id: String = ""
        override var mode: Int = Result.UNDEFINED
    }

    @Parcelize
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
        var _telephoneNumber: String = "",

        var _lat: String = "",
        var _lng: String = "",

        var marketList: MutableList<Market> = mutableListOf(),
        var _urls: MutableList<String> = mutableListOf(),
        var _knownClientIds: MutableList<String> = mutableListOf(),

        ) : UiState(), Parcelable


    data class ProductList(val list: ArrayList<Article> = ArrayList()) : UiState() {
        override var id: String = ""
        override var mode: Int = -1
    }

    @Parcelize
    data class Market(
        var name: String = "",
        var street: String = "",
        var houseNumber: String = "",
        var city: String = "",
        var zipCode: String = "",
        var dayOfWeek: String = "",
        var begin: String = "",
        var end: String = "",
        var dayIndicator: Int = UNDEFINED,
        override var id: String = UUID.randomUUID().toString(),
        override var mode: Int = UNDEFINED
    ) : UiState(), Parcelable {

        fun isItGood(): Boolean {
            return name.isNotEmpty() &&
            street.isNotEmpty() &&
            houseNumber.isNotEmpty() &&
            city.isNotEmpty() &&
            zipCode.isNotEmpty() &&
            dayOfWeek.isNotEmpty() &&
            begin.isNotEmpty() &&
            end.isNotEmpty() &&
            dayIndicator != UNDEFINED
        }

        fun isGoodTiming(): Boolean {
            return cleanBegin.replace(":","").toInt() <
                    cleanEnd.replace(":","").toInt()
        }

        val cleanBegin: String
        get() {  return begin.replace(" Uhr", "")}

        val cleanEnd: String
        get() {  return end.replace(" Uhr", "")}
    }


}