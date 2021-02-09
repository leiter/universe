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
    fun setupProviderConnection(): Observable<Result.SellerProfile>
    fun sendOrder(order: Result.Order, update: Boolean  = false): Single<Boolean>
    fun loadOrders(): Single<List<Result.Order>>
    fun clearUserData(): Single<Boolean>
    fun loadExistingOrder(orderId: String): Single<Result.Order>
    fun saveBuyerProfile(buyerProfile: Result.BuyerProfile): Single<Boolean>
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

    override fun setupProviderConnection(): Observable<Result.SellerProfile> {
        return Database.sellerProfile("").limitToFirst(1).getSingle().toObservable()
            .subscribeOn(Schedulers.io())
            .map {
                val sellerId = it.children.first().key!!
                val sellerProfile = it.child(sellerId).getValue(Result.SellerProfile::class.java)!!
                sellerProfile.id = sellerId
                sellerProfile
            }.observeOn(AndroidSchedulers.mainThread())
    }

    override fun sendOrder(order: Result.Order, update: Boolean): Single<Boolean> {
        return wrapInConnectionCheck {
                val date = order.pickUpDate.toOrderId()
                Database.orders().child(date).getSingleExists().flatMap { exists ->
                    if (exists.not() || update) {
                        return@flatMap Database.orders().child(date).setValue(order).getSingle()
                    } else {
                        Single.error(AlreadyPlaceOrder())
                    }
                }
        }
    }

    override fun loadOrders(): Single<List<Result.Order>> {
        return wrapInConnectionCheck { Database.orders().limitToLast(10).getSingleList() }
    }

    override fun clearUserData(): Single<Boolean> {
        return wrapInConnectionCheck {
            Database.orders().removeValue().getSingle()
            .zipWith(Database.buyer().removeValue().getSingle())
                .map { it.second && it.first } }
    }

    override fun loadExistingOrder(orderId: String): Single<Result.Order> {
        return wrapInConnectionCheck { Database.orders().child(orderId).getSingleValue() }
    }

    override fun saveBuyerProfile(buyerProfile: Result.BuyerProfile): Single<Boolean> {
        return wrapInConnectionCheck { Database.buyer().setValue(buyerProfile).getSingle() }
    }

    override fun loadBuyerProfile(): Single<Result.BuyerProfile> {
        return  Database.buyer().getSingleExists().flatMap {
            if(it) Database.buyer().getSingleValue() else Single.just(Result.BuyerProfile()) }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    private inline fun <reified T> wrapInConnectionCheck(crossinline func: () -> Single<T>): Single<T> {
        return Database.connectedStatus().checkConnected().subscribeOn(Schedulers.io())
            .flatMap { if(it) func(); else Single.error(NoInternetConnection()) }
    }

}
