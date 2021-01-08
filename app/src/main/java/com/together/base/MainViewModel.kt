package com.together.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getCompletable
import io.reactivex.disposables.CompositeDisposable


class MainViewModel : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    init {
        // wire DataSource to UiState

        disposable.add( MainMessagePipe.mainThreadMessage.subscribe {
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
            }
        })
    }

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

    val profile : UiState.SellerProfile = UiState.SellerProfile()

    val editProduct: MutableLiveData<UiState.Article> by lazy {
        MutableLiveData<UiState.Article>().also {
            it.value = UiState.Article()
        }
    }

    val markets: MutableLiveData<MutableList<UiState.Market>> by lazy {
        MutableLiveData<MutableList<UiState.Market>>().also {
            it.value = mutableListOf()
        }
    }

    fun deleteProduct(){
        blockingLoaderState.value = UiState.Loading
        editProduct.value?.let {  Database.articles().child(it._id).removeValue()
            .getCompletable().subscribe({success ->
                if(success) {
                    blockingLoaderState.value = UiState.LoadingDone
                }else {
                    blockingLoaderState.value = UiState.LoadingDone
                }
            }, {
                blockingLoaderState.value = UiState.LoadingDone
            })
        }
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}

