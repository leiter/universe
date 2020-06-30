package com.together.base

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize



enum class UnitNames(var unitName: String){

    KILO_GRAM("kg"),
    PIECE("Stück"),
    BUNCH("Bund"),
    BOWL("Schale"),
    UNIT_UNDEFINED("n/a");

}


sealed class UiState {

    abstract var id: String
    abstract var mode: Int

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
        override var id = "StateLoggedOut"
        override var mode: Int = MOVED

    }

    object BASE_AUTH : UiState() {
        override var id = "StateLoggedIn"
        override var mode: Int = MOVED

    }

    data class Article(
        override var id: String = "",
        var productId: Int = -1,
        var productName: String = "",
        var productDescription: String? = null,
        var available: Boolean = false,
        var units: List<Unit> = emptyList(),
        var remoteImageUrl: String = "",
        var localImageUrl: String = "",
        var discount: Long = 0L,

        var pricePerUnit: String = "",
        var unit: String = "",

        var price: String = "",
        var amount: String = "",

        override var mode: Int = MOVED

    ) : UiState()


    data class NewProductImage(val uri: Uri)

    data class SellerProfile(

        override var id: String = "",

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

        var urls: MutableList<String> = mutableListOf(),

        var knownClientIds: MutableList<String> = mutableListOf(),
        override var mode: Int = MOVED


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
                      override var id: String = "",
                      override var mode: Int = MOVED
    ) : UiState(), Parcelable


    data class ChatMessage(
        override var id: String = "",
        var creatorId: String = "",
        var name: String = "",
        var text: String = "",
        var photoUrl: String = "",
        override var mode: Int = MOVED

    ) : UiState()

    data class PostAnyMessage(
        override var id: String = "", var creatorId: String,
        var name: String = "",
        var text: String = "",
        var photoUrl: String = "",
        override var mode: Int = MOVED

    ) : UiState()


    data class Unit(

        var name: String = "",
        var price: String = "",
        var averageWeight: String = "",

        override var id: String = "",
        override var mode: Int = UNIT_UNDEFINED

    ) : UiState()


}