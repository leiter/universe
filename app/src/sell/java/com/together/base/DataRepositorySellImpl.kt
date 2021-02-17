package com.together.base

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.together.repository.Database
import com.together.repository.auth.FireBaseAuth
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import com.together.repository.Result
import com.together.repository.storage.*


class DataRepositorySellImpl constructor() : DataRepositorySell {

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

    override fun deleteProduct(product: Result.Article): Single<Boolean> {
        val location = try {
            FirebaseStorage.getInstance().getReferenceFromUrl(product.imageUrl)
        } catch (e: Exception) {
            null
        }
        return location?.delete()?.getSingle()?.flatMap {
            Database.articles().child(product.id).removeValue().getSingle()
        }
            ?: Database.articles().child(product.id).removeValue().getSingle()
    }

    override fun loadNextOrders(): Single<List<Result.Order>> {
        return Database.nextOrders().getSingle().map {
            val result = ArrayList<Result.Order>()
            it.children.forEach {
                it.children.forEach { order ->
                    val i = order.getValue(Result.Order::class.java)
                    i?.let {  result.add(i) }
                }
            }
            result.sortedByDescending { it.pickUpDate }.reversed()
        }
    }

    override fun loadSellerProfile(): Single<Result.SellerProfile> {
        return Database.sellerProfile(seller = true).getSingleExists().flatMap {
            if (it) Database.sellerProfile(seller = true).getSingleValue<Result.SellerProfile>()
            else Single.just(Result.SellerProfile())
        }
    }

    override fun uploadProduct(
        file: Single<File>,
        fileAttached: Boolean,
        product: Result.Article
    ): Single<Task<Void>> {
        val start = if (fileAttached)
            file.flatMap {
                val uri = Uri.fromFile(it)
                Database.storage().putFile(uri).getSingle()
            }.flatMap {
                it.metadata?.reference?.downloadUrl?.getTypedSingle()
            }.map {
                product.imageUrl = it.toString()
                product
            } else Single.just(product)
        return start.subscribeOn(Schedulers.io()).map { Database.articles().push().setValue(it) }
    }

}