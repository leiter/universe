package com.together.app

sealed class UiState {

    object LOGGEDOUT : UiState()

    object LOGGEDIN : UiState()

    data class Article(
        var productId: Int = -1,
        var productName: String = "",
        var productDescription: String? = null,
        var available: Boolean = false,
        var unit: String = "",  // Bund, Stück, Kg,
        var pricePerUnit: String = "",
        var imageUrl: String = ""
    ) : UiState()


    data class ChatMessage(var id: String = "",
                           var creatorId: String = "",
                           var name: String = "",
                           var text: String = "",
                           var photoUrl: String = "") : UiState()

    data class PostAnyMessage(var creatorId: String,
                               var name: String,
                               var text: String,
                               var photoUrl: String) : UiState()


    data class PostAnyMessages(var list: MutableList<PostAnyMessage>)






}