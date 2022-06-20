package com.together.base

import com.together.repository.AlreadyPlaceOrder
import com.together.repository.Database
import com.together.repository.NoInternetConnection
import com.together.repository.Result
import com.together.repository.storage.*
import com.together.utils.toOrderId
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit


interface DataRepository {
    fun setupProductConnection(): Observable<Result.Article>
    fun setupProviderConnection(): Single<Result.SellerProfile>
    fun sendOrder(
        order: Result.Order,
        update: Boolean = false,
        buyerProfile: Result.BuyerProfile
    ): Single<Result.BuyerProfile>

    fun loadOrders(
        sellerId: String,
        placedOrderIds: Map<String, String>
    ): Single<List<Result.Order>>

    fun clearUserData(
        sellerId: String,
        buyerProfile: Result.BuyerProfile
    ): Single<Result.CleanUpResult>

    fun loadExistingOrder(
        sellerId: String,
        orderId: String,
        orderPath: String
    ): Single<Result.Order>

    fun cancelOrder(
        sellerId: String,
        date: String,
        orderId: String,
    ): Single<Boolean>

    fun saveBuyerProfile(buyerProfile: Result.BuyerProfile): Single<Result.BuyerProfile>
    fun loadBuyerProfile(): Single<Result.BuyerProfile>
}

class DataRepositoryImpl : DataRepository {
    override fun setupProductConnection(): Observable<Result.Article> {
        return Database.sellerProfile("").limitToFirst(1).getSingle().toObservable()
            .subscribeOn(Schedulers.io())
            .map { it.children.first().key!! }
            .flatMap { Database.providerArticles(it).getObservable<Result.Article>() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun setupProviderConnection(): Single<Result.SellerProfile> {
        return Database.sellerProfile("").limitToFirst(1).getSingle()
            .subscribeOn(Schedulers.io())
            .map {
                val sellerId = it.children.first().key!!
                val sellerProfile = it.child(sellerId).getValue(Result.SellerProfile::class.java)!!
                sellerProfile.id = sellerId
                sellerProfile
            }.observeOn(AndroidSchedulers.mainThread())
    }

    override fun sendOrder(
        order: Result.Order,
        update: Boolean,
        buyerProfile: Result.BuyerProfile
    ): Single<Result.BuyerProfile> {
        return wrapInConnectionCheck {
            val date = order.pickUpDate.toOrderId()
            order.buyerProfile = buyerProfile
            val alreadyPlaceOrder = buyerProfile.placedOrderIds.keys.contains(date)
            val runMe = !alreadyPlaceOrder || update
            if (runMe) {
                val ref = Database.orderSeller(order.sellerId).child(date)
                val k =
                    if (!alreadyPlaceOrder) ref.push() else ref.child(buyerProfile.placedOrderIds[date]!!)
                k.setValue(order).getSingle().zipWith(Single.just(Pair(date, k.key!!)))
            } else {
                Single.error(AlreadyPlaceOrder())
            }.map { pair ->
                val m = buyerProfile.placedOrderIds.toMutableMap()
                m[pair.second.first] = pair.second.second
                buyerProfile.placedOrderIds = m
                if (order.buyerProfile.displayName != buyerProfile.displayName) {
                    buyerProfile.displayName = order.buyerProfile.displayName
                }
                buyerProfile.copy()
            }.flatMap { saveBuyerProfile(it) }
        }
    }

    override fun loadOrders(
        sellerId: String,
        placedOrderIds: Map<String, String>
    ): Single<List<Result.Order>> {
        return wrapInConnectionCheck {
            val list = placedOrderIds.map { item ->
                oneOrder(sellerId, item.key, item.value)
                    .subscribeOn(Schedulers.io())
            }
            Observable.fromIterable(list).flatMap { it.toObservable() }.toList()
        }
    }

    private fun Long.canBeCanceled(): Boolean {
        return TimeUnit.MILLISECONDS.toDays(this - Date().time) > 1
    }


    private fun oneOrder(sellerId: String, date: String, orderId: String): Single<Result.Order> {
        return Database.orderSeller(sellerId)
            .child(date).child(orderId).getSingleValue<Result.Order>()
            .map {
                if (it.pickUpDate.canBeCanceled())
                    it.id = orderId;
                it
            }
    }

    override fun clearUserData(
        sellerId: String,
        buyerProfile: Result.BuyerProfile
    ): Single<Result.CleanUpResult> {
        return wrapInConnectionCheck {
            var cleanUpResult = Result.CleanUpResult()
            if (buyerProfile.placedOrderIds.isEmpty()) {
                return@wrapInConnectionCheck Database.buyer().getSingleExists()
                    .flatMap { buyExists ->
                        if (buyExists) Database.buyer().removeValue().getSingle().map {
                            cleanUpResult.copy(profileDeleted = it)
                        } else Single.just(cleanUpResult.copy(profileDeleted = true))
                    }
            } else {
                cleanUpResult = cleanUpResult.copy(placedOrderIds = buyerProfile.placedOrderIds)
                Observable.fromIterable(buyerProfile.placedOrderIds.entries).flatMap { entry ->
                    deleteOrder(sellerId, entry.key, entry.value).toObservable()
                }.toList().map { cleanUpResult.copy(deletedOrders = it.toList()) }.flatMap {
                    Database.buyer().getSingleExists().flatMap { buyerExist ->
                        if (buyerExist) {
                            Database.buyer().removeValue().getSingle()
                                .map { cleanUpResult.copy(profileDeleted = it) }
                        } else Single.just(cleanUpResult.copy(profileDeleted = true))
                    }
                }
            }
        }
    }

    private fun deleteOrder(sellerId: String, orderId: String, orderPath: String): Single<String> {
        return Database.orderSeller(sellerId).child(orderId).child(orderPath)
            .removeValue().getSingle().subscribeOn(Schedulers.io())
            .map { if (it) orderPath else "" }
    }

    override fun loadExistingOrder(
        sellerId: String,
        orderId: String,
        orderPath: String
    ): Single<Result.Order> {
        return wrapInConnectionCheck {
            Database.orderSeller(sellerId).child(orderId).child(orderPath)
                .getSingleValue<Result.Order>().subscribeOn(Schedulers.io())
        }
    }

    override fun cancelOrder(sellerId: String, date: String, orderId: String): Single<Boolean> {
        return deleteOrder(sellerId,date, orderId).map { it != "" }

    }

    override fun saveBuyerProfile(buyerProfile: Result.BuyerProfile): Single<Result.BuyerProfile> {
        return wrapInConnectionCheck {
            Database.buyer().setValue(buyerProfile).getSingle()
                .map { it ->
                    buyerProfile
                }
        } // fixMe fail in case
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun loadBuyerProfile(): Single<Result.BuyerProfile> {
        return Database.buyer().getSingleExists().subscribeOn(Schedulers.io()).flatMap {
            if (it) Database.buyer().getSingleValue()
            else Single.just(Result.BuyerProfile())
        }.observeOn(AndroidSchedulers.mainThread())
    }

    private inline fun <reified T> wrapInConnectionCheck(crossinline func: () -> Single<T>): Single<T> {
        return Database.connectedStatus().checkConnected().subscribeOn(Schedulers.io())
            .flatMap { if (it) func(); else Single.error(NoInternetConnection()) }
    }

}
