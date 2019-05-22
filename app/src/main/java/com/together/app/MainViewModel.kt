package com.together.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import io.reactivex.disposables.Disposable

sealed class LoggedState {
    class OUT : LoggedState()
    class IN : LoggedState()
}


class MainViewModel : ViewModel() {

    var disposable: Disposable

    init {
        disposable = MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is LoggedOut -> loggedState.value = LoggedState.OUT()
                is LoggedIn -> loggedState.value = LoggedState.IN()
            }

        }
    }

    val loggedState: MutableLiveData<LoggedState> = MutableLiveData()

    val currentChat: MutableLiveData<ChatThread> = MutableLiveData()



}