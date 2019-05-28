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

    class ArticleList(list: List<Result.Article>) : UiState() {

        val allArticles: MutableList<Article> = mutableListOf()

        init {
            list.forEach {
                allArticles.add(
                    Article(
                        it.productId,
                        it.productName,
                        it.imageUrl
                    )
                )
            }
        }

        val availableArticles: List<Article>
            get() {
                return allArticles.filter { it.available }
            }
    }

    data class PostChatMessage(val creatorId: String,
                               val name: String,
                               val text: String,
                               val photoUrl: String) : UiState()


}