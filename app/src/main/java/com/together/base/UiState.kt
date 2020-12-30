package com.together.base

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.NumberFormat

enum class UnitNames(var unitName: String){

    KILO_GRAM("kg"),
    PIECE("Stück"),
    BUNCH("Bund"),
    BOWL("Schale"),
    UNIT_UNDEFINED("n/a");

}


sealed class UiState {

    abstract var _id: String
    abstract var _mode: Int

    companion object {

        const val ADDED = 0
        const val CHANGED = 1
        const val MOVED = 2
        const val REMOVED = 3
        const val UNDEFINED = -1
        
        const val KILO_GRAM = 0
        const val GRAM = 1
        const val PIECE = 2
        const val BUNCH = 3
        const val BOWL = 4
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
        var units: List<Unit> = emptyList(),
        var remoteImageUrl: String = "",
        var localImageUrl: String = "",
        var discount: Long = 0L,
        var amount: String = "",
        var pricePerUnit: String = "",
        var unit: String = "",
        var amountCount: Long = 0L,
        var price: String = "",


        override var _mode: Int = MOVED

    ) : UiState(){
        var amountDisplay: String = "$amountCount $unit"
//        var displayPrice: String = preparePrice()
//        private fun preparePrice(): String {
//            return if(amount.isNotEmpty()){
//            val v = NumberFormat.getInstance().parse(amount)!!
//            var i = amountCount *
//                    NumberFormat.getInstance().parse(
//                        pricePerUnit)!!.toDouble()
//            i = Math.round(i * 100.0) / 100.0
//            "%.2f€".format(i)}
//            else "0,00€"
//        }
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