package com.together.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.repository.Result
import io.reactivex.disposables.CompositeDisposable


class MainViewModel : ViewModel() {

    var disposable: CompositeDisposable = CompositeDisposable()


    init {
        // wire DataSource to UiState

        disposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {

                is Result.LoggedOut ->
                    loggedState.value = UiState.LOGGEDOUT
                is Result.LoggedIn ->
                    loggedState.value = UiState.LOGGEDIN

                is Result.NewImageCreated -> {
                    if (newProduct.value == null)
                        newProduct.value = UiState.NewProductImage(it.uri)
                    newProduct.value = UiState.NewProductImage(it.uri)
                }
            }
        })
    }

    val loggedState: MutableLiveData<UiState> = MutableLiveData()

    val focusedProduct: MutableLiveData<UiState.Article> = MutableLiveData()

    val createProduct: MutableLiveData<UiState.Article> = MutableLiveData()

    val newProduct: MutableLiveData<UiState.NewProductImage> = MutableLiveData()


    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }


}

