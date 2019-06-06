package com.together.app

import com.together.repository.Result

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

    class ProductList(list: List<Result.Article>) : UiState() {

        val allProducts: MutableList<Article> = mutableListOf()

        init {
            list.forEach {
                allProducts.add(
                    Article(
                        it.productId,
                        it.productName,
                        it.imageUrl
                    )
                )
            }
        }

        val availableProducts: List<Article>
            get() {
                return allProducts.filter { it.available }
            }
    }

    data class ChatMessage(var creatorId: String,
                           var name: String,
                           var text: String,
                           var photoUrl: String) : UiState()

    data class PostAnyMessage(var creatorId: String,
                               var name: String,
                               var text: String,
                               var photoUrl: String) : UiState()


}