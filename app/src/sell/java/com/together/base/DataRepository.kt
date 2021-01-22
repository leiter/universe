package com.together.base

import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getObservable
import com.together.repository.storage.getSingle
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface DataRepository {

    fun setupProductConnection(): Observable<Result.Article>
    fun uploadSellerProfile(profile: Result.SellerProfile): Single<Boolean>
    fun deleteProduct(productId: String): Single<Boolean>
}

class DataRepositoryImpl : DataRepository {

    override fun setupProductConnection(): Observable<Result.Article> {
        return Database.articles().getObservable<Result.Article>()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun uploadSellerProfile(profile: Result.SellerProfile): Single<Boolean> {
        val z = profile.copy(markets = profile.markets, sellerId = FireBaseAuth.getAuth().uid!!)
        return Database.sellerProfile("", true).setValue(z)
            .getSingle().subscribeOn(Schedulers.io())
    }

    override fun deleteProduct(productId: String): Single<Boolean> {
        return Database.articles().child(productId).removeValue()
            .getSingle().subscribeOn(Schedulers.io())
    }

}