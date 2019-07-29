package com.together.app

import android.net.Uri

sealed class UiState {

    abstract var id : String

    object LOGGEDOUT : UiState(){
        override var id = "StateLoggedOut"
    }

    object LOGGEDIN : UiState() {
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

    data class ChatMessage(override var id: String = "",
                           var creatorId: String = "",
                           var name: String = "",
                           var text: String = "",
                           var photoUrl: String = "") : UiState()

    data class PostAnyMessage(
        override var id: String = "", var creatorId: String,
        var name: String = "",
        var text: String = "",
        var photoUrl: String = "") : UiState()


    data class PostAnyMessages(var list: MutableList<PostAnyMessage>)






}