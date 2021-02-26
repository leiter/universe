package com.together.base

import com.google.android.gms.tasks.Task
import com.together.repository.Result
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File


interface DataRepositorySell {

    fun setupProductConnection(): Observable<Result.Article>
    fun uploadSellerProfile(profile: Result.SellerProfile): Single<Boolean>
    fun deleteProduct(product: Result.Article): Single<Boolean>
    fun loadNextOrders(): Single<List<Result.Order>>
    fun loadSellerProfile(): Single<Result.SellerProfile>
    fun uploadProduct(
        file: Single<File>,
        fileAttached: Boolean,
        product: Result.Article
    ): Single<String>

}

