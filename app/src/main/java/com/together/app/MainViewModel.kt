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

                is Result.Article -> productList.value?.add(UiState.Article())

                is Result.ArticleList -> throw Exception("TTTTTTTTTTTTTTTTT")

                is Result.ChatThread -> throw Exception("TTTTTTTTTTTTTTTTT")





            }
        })
    }



    val loggedState: MutableLiveData<UiState> = MutableLiveData()



    val openChat: MutableLiveData<MutableList<UiState.ChatMessage>> = MutableLiveData()

    val productList: MutableLiveData<MutableList<UiState.Article>> = MutableLiveData()



    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}

