package com.together.repository


// Results are used for Requests and Results this will probably change in future
sealed class Result {

    object LoggedOut : Result()

    object LoggedIn : Result()

    data class FireDatabaseError(
        var code: Int = Int.MAX_VALUE,
        var message: String = "",
        var detail: String = ""
    ) : Result()


    data class User(
        var id: String = "",
        var displayName: String = "",
        var emailAdress: String = "",
        var chatThreadIds: List<String> = listOf()
    ) : Result()


    data class Article(
        var productId: Int = -1,
        var productName: String = "",
        var productDescription: String? = null,
        var available: Boolean = false,
        var unit: String = "",
        var pricePerUnit: String = "",
        var imageUrl: String = ""
    ) : Result()


    data class Order(
        var user: User,
        var articles: List<Article>
    ) : Result()


    object DeviceData {
        var androidSdkVersion: Int = 1
    }

    data class ArticleList(
        var articles: List<Article>
    ) : Result()

    data class ChatMessage(
        val creatorId: String,
        val name: String,
        val text: String,
        val photoUrl: String
    ) : Result()


    data class ChatThread (
        var id: String = "",
        var name: String = "",
        var userIds: List<String> = emptyList(),
        var messageList: List<ChatMessage> = emptyList(),
        var photoUrl: String = ""

    ) : Result()
}








