package com.together.base

import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.storage.getObservable
import com.together.repository.storage.getSingle
import com.together.utils.dataSellerToUi
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


interface DataRepository {
    fun setupProductConnection(): Observable<Result.Article>
    fun setupProviderConnection(): Observable<Result.SellerProfile>
    fun sendOrder(order: Result.Order): Single<Boolean>
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
                it.child(sellerId).getValue(Result.SellerProfile::class.java)!!
            }.observeOn(AndroidSchedulers.mainThread())
    }

    override fun sendOrder(order: Result.Order): Single<Boolean> {
        return Database.orders().setValue(order)
            .getSingle().subscribeOn(Schedulers.io())
    }


}
