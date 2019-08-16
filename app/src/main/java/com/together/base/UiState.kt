package com.together.base

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class UiState {

    abstract var id: String

    object LOGGEDOUT : UiState() {
        override var id = "StateLoggedOut"
    }

    object BASE_AUTH : UiState() {
        override var id = "StateLoggedIn"
    }

    data class Article(
        override var id: String = "",
        var productId: Int = -1,
        var productName: String = "",
        var productDescription: String? = null,
        var available: Boolean = false,
        var unit: String = "",  // Bund, Stück, Kg,
        var pricePerUnit: String = "",
        var remoteImageUrl: String = "",
        var localImageUrl: String = ""

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

        var knownClientIds: MutableList<String> = mutableListOf()

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
                      override var id: String = "") : UiState(), Parcelable


    data class ChatMessage(
        override var id: String = "",
        var creatorId: String = "",
        var name: String = "",
        var text: String = "",
        var photoUrl: String = ""
    ) : UiState()

    data class PostAnyMessage(
        override var id: String = "", var creatorId: String,
        var name: String = "",
        var text: String = "",
        var photoUrl: String = ""
    ) : UiState()


}