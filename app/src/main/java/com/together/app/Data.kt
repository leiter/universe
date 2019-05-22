package com.together.app

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*


interface BaseModel {
    val remoteId: Int
    val createdAt: Date
    var modifiedAt: Date
    //type
}

sealed class ResultMessage


class LoggedOut(
    override val remoteId: Int,
    override val createdAt: Date,
    override var modifiedAt: Date
) : ResultMessage(), BaseModel

class LoggedIn(
    override val remoteId: Int,
    override val createdAt: Date,
    override var modifiedAt: Date
) :
    ResultMessage(), BaseModel

data class Supply(
    val articles: List<Article>,
    override val remoteId: Int,
    override val createdAt: Date,
    override var modifiedAt: Date
) : ResultMessage(), BaseModel

data class Articles(
    val stuff: Da,
    override val remoteId: Int,
    override val createdAt: Date,
    override var modifiedAt: Date
) : ResultMessage(), BaseModel


data class ChatThread(
    val id: String,
    val name: String,
    val userIds: List<String>,
    val messageList: List<ChatMessage>,
    val photoUrl: String
)


data class ChatMessage(
    val id: String,
    val text: String,
    val name: String,
    val photoUrl: String
)


data class User(
    override val remoteId: Int,
    override val createdAt: Date,
    override var modifiedAt: Date,
    val userId: Int,
    val roles: List<Role>

) : BaseModel


data class Order(
    override val remoteId: Int,
    override val createdAt: Date,
    override var modifiedAt: Date,
    val user: User,
    val articles: List<Article>
) : BaseModel


data class Article(
    val productId: Int,
    val productName: String,
    val productDescription: String?,
    val amount: Amount,
    val imageUrl: String,
    var available: Boolean
)

object DeviceData {
    val androidSdkVersion: Int = 1
}


sealed class Amount {
    class Hands(var amount: Long) : Amount()
    class Weight(var amount: Long) : Amount()
    class LooseWeight(var amount: Long) : Amount()
    class Count(var amount: Long) : Amount()

}

sealed class Role {
    val buyer = 0
    val seller = 1

    class Buyer(val me: Int = 0) : Role()
    class Seller(val me: Int = 1) : Role()
}

@IgnoreExtraProperties
data class Da(
    val name: String = "",
    val category: String = "",
    override var remoteId: Int = -1,
    override var createdAt: Date = Date(),
    override var modifiedAt: Date = Date()
) : ResultMessage() , BaseModel
