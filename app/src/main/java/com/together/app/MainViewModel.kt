package com.together.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.repository.Result
import io.reactivex.disposables.CompositeDisposable


class MainViewModel : ViewModel() {

    val productList: MutableLiveData<MutableList<UiState.Article>>

    var disposable: CompositeDisposable = CompositeDisposable()

    init {
        // wire DataSource to UiState

        productList = MutableLiveData()
        productList.value = mutableListOf()

        disposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {

                is Result.LoggedOut ->
                    loggedState.value = UiState.LOGGEDOUT
                is Result.LoggedIn ->
                    loggedState.value = UiState.LOGGEDIN

                is Result.Article -> productList.value?.add(UiState.Article(productName = it.productName,
                    productDescription = it.productDescription, imageUrl = it.imageUrl))

                is Result.ChatThread -> throw Exception("TTTTTTTTTTTTTTTTT")


            }
        })
    }


    val loggedState: MutableLiveData<UiState> = MutableLiveData()

    val openChat: MutableLiveData<MutableList<UiState.ChatMessage>> = MutableLiveData()


    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}

