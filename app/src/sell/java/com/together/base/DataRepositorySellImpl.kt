package com.together.base

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.together.repository.Database
import com.together.repository.NoInternetConnection
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.*
import com.together.utils.getMarketDates
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject


class DataRepositorySellImpl @Inject constructor() : DataRepositorySell {

    override fun setupProductConnection(): Observable<Result.Article> {
        return Database.articles().getObservable<Result.Article>()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun uploadSellerProfile(profile: Result.SellerProfile): Single<Boolean> {
        val z = profile.copy(markets = profile.markets, sellerId = FireBaseAuth.getAuth().uid!!)
        return wrapInConnectionCheck {  Database.sellerProfile("", true).setValue(z)
            .getSingle() }.observeOn(AndroidSchedulers.mainThread())
    }

    override fun deleteProduct(product: Result.Article): Single<Boolean> {
        val location = try {
            FirebaseStorage.getInstance().getReferenceFromUrl(product.imageUrl)
        } catch (e: Exception) {
            null
        }
        return wrapInConnectionCheck {  location?.delete()?.getSingle()?.flatMap {
            Database.articles().child(product.id).removeValue().getSingle()
        } ?: Database.articles().child(product.id).removeValue().getSingle() }
    }

    override fun loadNextOrders(): Observable<List<Result.Order>> {
        val orderList = ArrayList<Result.Order>()
        return loadSellerProfile().map { it.getMarketDates() }.toObservable()
            .flatMapIterable { it }.map { Database.nextOrders(it) }
            .flatMap { it.getObservable() }
            .map { order ->
                val i = order.getValue(Result.Order::class.java)
                i?.id = order.key!!
                i?.let {
                    val o = orderList.find { it.id == i.id }
                    o?.let{ orderList.remove(o) }
                    orderList.add(i)
                }
                orderList
            }.map { it.sortedByDescending { order -> order.pickUpDate }.reversed() }
    }

    override fun loadSellerProfile(): Single<Result.SellerProfile> {
        return Database.sellerProfile(seller = true).getSingleExists().subscribeOn(Schedulers.io())
            .flatMap {
            if (it) Database.sellerProfile(seller = true).getSingleValue<Result.SellerProfile>()
            else Single.just(Result.SellerProfile())
        }
    }

    override fun uploadProduct(
        file: Single<File>,
        fileAttached: Boolean,
        product: Result.Article
    ): Single<Result.Article> {
        val deleteOldFile =  try {
            FirebaseStorage.getInstance().getReferenceFromUrl(product.imageUrl)
        } catch (e: Exception) {
            null
        }
        val start = if (fileAttached)
            file.flatMap { localFile ->
                val uri = Uri.fromFile(localFile)
                Database.storage().putFile(uri).getSingle()
            }.flatMap {
                it.metadata?.reference?.downloadUrl?.getTypedSingle()
            }.map {
                product.imageUrl = it.toString()
                product
            } else Single.just(product)
        return wrapInConnectionCheck {
            start.subscribeOn(Schedulers.io()).map { result ->
                val id: String
                if (result.id.isEmpty()) {
                    val ref = Database.articles().push()
                    id = ref.key!!
                    val r = result.copy(id = id)
                    Pair(ref.setValue(result), r)
                } else {
                    id = result.id
                    Pair(Database.articles().child(id).setValue(result), result)
                }
            }.map {
                if (fileAttached) deleteOldFile?.delete()
                it.second
            }
        }
    }

    private inline fun <reified T> wrapInConnectionCheck(crossinline func: () -> Single<T>): Single<T> {
        return Database.connectedStatus().checkConnected().subscribeOn(Schedulers.io())
            .flatMap { if (it) func(); else Single.error(NoInternetConnection()) }
    }

}