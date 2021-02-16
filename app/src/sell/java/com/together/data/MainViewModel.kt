package com.together.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.data.UiEvent.Companion.DELETE_PRODUCT
import com.together.data.UiEvent.Companion.UNDEFINED
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.utils.dataArticleToUi
import com.together.utils.uiArticleToData
import io.reactivex.Single
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

class MainViewModel(private val dataRepository: DataRepositorySell = DataRepositorySellImpl()) :
    ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    init {
        disposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is Result.LoggedOut ->
                    loggedState.value = UiState.LoggedOut

                is Result.LoggedIn ->
                    loggedState.value = UiState.BaseAuth()

                is Result.NewImageCreated -> {
                    if (newProduct.value == null)
                        newProduct.value = UiState.NewProductImage(it.uri!!)
                    else newProduct.value = UiState.NewProductImage(it.uri!!)
                }
            }
        })
    }

    private val productData: MutableLiveData<MutableList<UiState.Article>> by lazy {
        disposable.add(
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

    var profile: UiState.SellerProfile = UiState.SellerProfile()

    val editProduct: MutableLiveData<UiState.Article> by lazy {
        MutableLiveData<UiState.Article>().also {
            it.value = UiState.Article()
        }
    }

    fun loadNextOrders() {
        dataRepository.loadNextOrders().subscribe()
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
                        blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
                    } else {
                        // msg
                        blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
                    }
                }, {
                    // msg
                    blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
                })
        }
    }

    fun uploadProduct(file: Single<File>, fileAttached: Boolean) {
        val product = editProduct.value!!.uiArticleToData()
        dataRepository.uploadProduct(file,fileAttached,product)
            .subscribe({

            },{

            }).addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

}

