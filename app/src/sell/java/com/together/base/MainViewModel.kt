package com.together.base

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.base.UiEvent.Companion.DELETE_PRODUCT
import com.together.base.UiEvent.Companion.UNDEFINED
import com.together.base.UiEvent.Companion.UPLOAD_PRODUCT
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.utils.dataArticleToUi
import com.together.utils.uiArticleToData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.io.File

fun MutableList<UiState.Article>.addItem(
    item: UiState.Article,
    productData: MutableLiveData<MutableList<UiState.Article>>
) {
    val index = indexOf(item)
    when (item.mode) {
        UiState.ADDED -> add(item)
        UiState.REMOVED -> removeAt(indexOf(first { it.id == item.id }))
        UiState.CHANGED -> {
            if (index == -1) {
                val i = indexOf(first { it.id == item.id })
                removeAt(i)
                add(i, item)
            } else {
                removeAt(index)
                add(index, item)
            }
        }
    }
    productData.value = this.toMutableList()
}

inline fun <reified T : UiState> MutableList<T>.addGenItem(
    item: T,
    productData: MutableLiveData<MutableList<T>>
) {
    val index = indexOf(item)
    when (item.mode) {
        UiState.ADDED -> add(item)
        UiState.REMOVED -> removeAt(indexOf(first { it.id == item.id }))
        UiState.CHANGED -> {
            if (index == -1) {
                val i = indexOf(first { it.id == item.id })
                removeAt(i)
                add(i, item)
            } else {
                removeAt(index)
                add(index, item)
            }
        }
    }
    productData.value = this.toMutableList()
}

class MainViewModel(private val dataRepository: DataRepositorySell = DataRepositorySellImpl()) :
    ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()
    private var disposable2: CompositeDisposable = CompositeDisposable()
    var uploadImage: Boolean = false

    init {
        disposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is Result.LoggedOut ->
                    loggedState.value = UiState.LoggedOut

                is Result.LoggedIn ->
                    loggedState.value = UiState.BaseAuth()

                is Result.NewImageCreated -> {
                    uploadImage = true
                    newProduct.value = UiState.NewProductImage(Uri.parse(it.uri!!))
                }
                is Result.SetDetailDescription -> {
                    editProduct.value?.detailInfo = it.text
                }
            }
        })
    }

    private val productData: MutableLiveData<MutableList<UiState.Article>> by lazy {
        disposable2.add(
            dataRepository.setupProductConnection()
                .subscribe({
                    val e = it.dataArticleToUi()
                    productData.value?.addItem(e, productData)

                }, { it.printStackTrace() },
                    { Log.e("Rx", "Complete called."); })

        )
        MutableLiveData(emptyList<UiState.Article>().toMutableList())
    }

    val productList: LiveData<MutableList<UiState.Article>>
        get() {
            return productData
        }

    val blockingLoaderState: MutableLiveData<UiEvent> by lazy {
        MutableLiveData<UiEvent>().also { it.value = UiEvent.LoadingDone(UNDEFINED) }
    }

    val loggedState: MutableLiveData<UiState> by lazy {
        MutableLiveData<UiState>().also { it.value = FireBaseAuth.isLoggedIn() }
    }

    val newProduct: MutableLiveData<UiState.NewProductImage> = MutableLiveData()

    var sellerProfile: UiState.SellerProfile = UiState.SellerProfile()

    val editProduct: MutableLiveData<UiState.Article> by lazy {
        MutableLiveData<UiState.Article>().also {
            it.value = UiState.Article()
        }
    }

    fun deleteProduct() {
        editProduct.value?.let {
            val id = it.id
            if (id.isEmpty()) return  // todo msg
            blockingLoaderState.value = UiEvent.Loading(0)
            val deleteMe = it.uiArticleToData()
            dataRepository.deleteProduct(deleteMe)
                .subscribe({ success ->
                    if (success) {
                        editProduct.value = UiState.Article()
                        blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
                    } else {
                        // msg
                        blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
                    }
                }, {
                    editProduct.value = UiState.Article()
                    // msg
                    blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
                })
        }
    }

    fun uploadProduct(file: Single<File>) {
        val product = editProduct.value!!.uiArticleToData()
        blockingLoaderState.value = UiEvent.Loading()
        dataRepository.uploadProduct(file, uploadImage, product)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                editProduct.value = it.dataArticleToUi()
                blockingLoaderState.value = UiEvent.LoadingDone(UPLOAD_PRODUCT)
            }, {
                blockingLoaderState.value = UiEvent.LoadingDone(UPLOAD_PRODUCT)
            }).addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    fun prepareLogout() {
        disposable2.clear()
    }

}

