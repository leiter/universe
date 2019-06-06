package com.together.repository




// Results are used for Requests and Results this will probably change in future
sealed class Result {

    abstract var id :String

    object LoggedOut : Result(){
        override var id: String = ""

    }

    object LoggedIn : Result() {
        override var id: String = ""

    }

    data class FireDatabaseError(
        override var id: String = "",

        var code: Int = Int.MAX_VALUE,
        var message: String = "",
        var detail: String = ""
    ) : Result()


    data class User(
        override var id: String = "",
        var displayName: String = "",
        var emailAdress: String = "",
        var chatThreadIds: List<String> = listOf()
    ) : Result()


    data class Article(
        override var id: String = "",
        var productId: Int = -1,
        var productName: String = "",
        var productDescription: String? = null,
        var available: Boolean = false,
        var unit: String = "",
        var pricePerUnit: String = "",
        var imageUrl: String = ""
    ) : Result()


    data class Order(
        override var id: String = "",

        var user: User,
        var articles: List<Article>
    ) : Result()


    object DeviceData {
        var androidSdkVersion: Int = 1
    }



    data class ChatMessage(
        override var id: String = "",

        val creatorId: String,
        val name: String,
        val text: String,
        val photoUrl: String
    ) : Result()


    data class ChatThread (
        override var id: String = "",
        var name: String = "",
        var userIds: List<String> = emptyList(),
        var messageList: List<ChatMessage> = emptyList(),
        var photoUrl: String = ""

    ) : Result()
}








