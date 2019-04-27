package com.together

import java.util.*


interface BaseModel {
    val remoteId: Int
    val createdAt: Date
    var modifiedAt: Date
    //type
}

data class User(
    override val remoteId: Int,
    override val createdAt: Date,
    override var modifiedAt: Date,

    val userId: Int,
    val roles: List<Role>

) : BaseModel


data class Order(
    val user: User,
    val articles: List<Article>
)

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
    class Buyer(val me: Int = 0) : Role()
    class Seller(val me: Int = 1) : Role()
}