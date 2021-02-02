package com.together.base

import com.together.repository.AlreadyPlaceOrder
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.storage.*
import com.together.utils.toOrderId
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalStateException


interface DataRepository {
    fun setupProductConnection(): Observable<Result.Article>
    fun setupProviderConnection(): Observable<Result.SellerProfile>
    fun sendOrder(order: Result.Order, update: Boolean  = false): Single<Boolean>
    fun loadOrders(): Single<List<Result.Order>>
    fun clearUserData(): Single<Boolean>
    fun loadExistingOrder(orderId: String): Single<Result.Order>
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
        return wrapInConnectionCheck { isConnected ->
            if (isConnected) {
                val date = order.pickUpDate.toOrderId()
                Database.orders().child(date).getSingleExists().flatMap { exists ->
                    if (exists.not() || update) {
                        return@flatMap Database.orders().child(date).setValue(order).getSingle()
                    } else {
                        Single.error(AlreadyPlaceOrder())
                    }
                }
            } else Single.error(IllegalStateException("No internet connection."))
        }
    }

    override fun loadOrders(): Single<List<Result.Order>> {
        return wrapInConnectionCheck { isConnected ->
            if (isConnected) Database.orders().orderByChild("isNotFavourite")
                .limitToLast(10).getSingleList()
            else Single.error(IllegalStateException("No internet connection."))
        }
    }

    override fun clearUserData(): Single<Boolean> {
        return wrapInConnectionCheck { isConnected ->
            if (isConnected) Database.orders().removeValue().getSingle()
            else Single.error(IllegalStateException("No internet connection."))
        }
    }

    override fun loadExistingOrder(orderId: String): Single<Result.Order> {
        return wrapInConnectionCheck { isConnected ->
            if(isConnected) Database.orders().child(orderId).getSingleValue()
            else Single.error(IllegalStateException("No internet connection."))
        }
    }

    private inline fun <reified T> wrapInConnectionCheck(crossinline func: (Boolean) -> Single<T>): Single<T> {
        return Database.connectedStatus().checkConnected().subscribeOn(Schedulers.io())
            .flatMap { func(it) }
    }

}
