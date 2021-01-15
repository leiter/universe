package com.together.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getCompletable
import com.together.repository.storage.getObservable
import com.together.repository.storage.getSingle
import com.together.repository.storage.getSingleValue
import com.together.utils.dataArticleToUi
import com.together.utils.dataSellerToUi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

fun MutableList<UiState.Article>.addItem(
    item: UiState.Article,
    productData: MutableLiveData<MutableList<UiState.Article>>
) {
    val index = indexOf(item)
    when (item._mode) {
        UiState.ADDED -> add(item)
        UiState.REMOVED -> removeAt(indexOf(first { it._id == item._id }))
        UiState.CHANGED -> {
            if (index == -1) {
                val i = indexOf(first { it._id == item._id })
                removeAt(i)
                add(i, item)
            } else {
                removeAt(index)
                add(index, item)
            }
        }
    }
    productData.value = this.toSet().toMutableList()
}


class MainViewModel : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    private val productData: MutableLiveData<MutableList<UiState.Article>> =
        MutableLiveData(emptyList<UiState.Article>().toMutableList())

    val productList: LiveData<MutableList<UiState.Article>>
        get() {
            return productData
        }

    val uiTasks: LiveData<out UiState> = MutableLiveData()

    lateinit var sellerProfile: UiState.SellerProfile

    var buyerProfile = UiState.BuyerProfile()

    var order = UiState.Order()

    init {

        disposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is Result.LoggedOut ->
                    loggedState.value = UiState.LOGGEDOUT

                is Result.LoggedIn ->
                    loggedState.value = UiState.BASE_AUTH

                is Result.NewImageCreated -> {
                    if (newProduct.value == null)
                        newProduct.value = UiState.NewProductImage(it.uri!!)
                    else newProduct.value = UiState.NewProductImage(it.uri!!)
                }
                is Result.ImageLoaded -> {
                    imageLoadingProgress.value = UiState.LoadingProgress(it.progressId, it.show)
                }
            }
        })

        disposable.add(
            Database.sellerProfile("").limitToFirst(1).getSingle().toObservable()
                .subscribeOn(Schedulers.io())
                .map { it.children.first().key!! }
                .flatMap { uid ->
                    Database.sellerProfile(uid).getSingle<Result.SellerProfile>().toObservable()
                        .concatMap {
                            sellerProfile = it.dataSellerToUi()
                            Database.providerArticles(uid).getObservable<Result.Article>()
                        }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val e = it.dataArticleToUi()
                    productData.value?.addItem(e, productData)
                    if (productData.value?.size == 1) {
                        presentedProduct.value = productData.value!![0]
                    }
                }, { it.printStackTrace() },
                    { Log.e("Rx", "Complete called."); })
        )
    }

    fun sendOrder() {
        if (loggedState.value == UiState.LOGGEDOUT) {
            loggedState.value = UiState.LOGIN_REQUIRED
            return
        }
    }

    val imageLoadingProgress = MutableLiveData<UiState.LoadingProgress>().also {
        it.value = UiState.LoadingProgress(-1, false)
    }
    var smsMessageText = ""

    val blockingLoaderState: MutableLiveData<UiState> by lazy {
        MutableLiveData<UiState>().also { it.value = UiState.LoadingDone }
    }

    val loggedState: MutableLiveData<UiState> by lazy {
        MutableLiveData<UiState>().also { it.value = FireBaseAuth.isLoggedIn() }
    }

    val presentedProduct: MutableLiveData<UiState.Article> = MutableLiveData()

    val newProduct: MutableLiveData<UiState.NewProductImage> = MutableLiveData()

    val basket: MutableLiveData<MutableList<UiState.Article>> by lazy {
        MutableLiveData<MutableList<UiState.Article>>().also {
            it.value = mutableListOf()
        }
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}

