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


interface DataRepository {
    fun setupProductConnection(): Observable<Result.Article>
    fun setupProviderConnection(): Single<Result.SellerProfile>
    fun sendOrder(
        order: Result.Order,
        update: Boolean = false,
        buyerProfile: Result.BuyerProfile
    ): Single<Result.BuyerProfile>
    fun loadOrders(): Single<List<Result.Order>>
    fun clearUserData(): Single<Boolean>
    fun loadExistingOrder(orderId: String): Single<Result.Order>
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
            val alreadyPlaceOrder = buyerProfile.placedOrderIds.keys.contains(date)
            val runMe = !alreadyPlaceOrder || update
//            Database.orderSeller(order.sellerId).child(date).getSingleExists().flatMap { exists ->
                if (runMe) {
                    val ref = Database.orderSeller(order.sellerId).child(date)
                    val k =  if (!alreadyPlaceOrder) ref.push() else ref.child(buyerProfile.placedOrderIds[date]!!)
                    k.setValue(order).getSingle().zipWith(Single.just(Pair(date,k.key!!)))
                } else {
                    Single.error(AlreadyPlaceOrder())
                }
//            }
            .map { pair ->
                val m = buyerProfile.placedOrderIds.toMutableMap()
                m[pair.second.first] = pair.second.second
                buyerProfile.placedOrderIds = m
                buyerProfile.copy()
            }.flatMap { saveBuyerProfile(it)  }
        }
    }

    override fun loadOrders(): Single<List<Result.Order>> {
        return wrapInConnectionCheck { Database.orderSeller("76qsfkWc4rYCY36D2Eq7dnLR6743").orderByKey().getSingleList() }
    }

    override fun clearUserData(): Single<Boolean> {
        return wrapInConnectionCheck {
            Database.orders().getSingleExists()
                .zipWith(Database.buyer().getSingleExists()).flatMap {
                    when {
                        it.first && it.second -> {
                            Database.orders().removeValue().getSingle()
                                .zipWith(Database.buyer().removeValue().getSingle()) }
                        it.first -> Database.orders().removeValue().getSingle()
                            .zipWith(Single.just(true))
                        it.second -> Database.buyer().removeValue().getSingle()
                            .zipWith(Single.just(true))
                        else -> Single.just(true).zipWith(Single.just(true))
                    }
                }.map { it.second && it.first }
        }
    }

    override fun loadExistingOrder(orderId: String): Single<Result.Order> {
        return wrapInConnectionCheck { Database.orderSeller("76qsfkWc4rYCY36D2Eq7dnLR6743").child(orderId).getSingleValue() }
    }

    override fun saveBuyerProfile(buyerProfile: Result.BuyerProfile): Single<Result.BuyerProfile> {
        return wrapInConnectionCheck { Database.buyer().setValue(buyerProfile).getSingle()
            .map { it ->
                buyerProfile
            } } // fixMe fail in case
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
