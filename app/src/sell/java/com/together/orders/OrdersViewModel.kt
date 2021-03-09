package com.together.orders

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.base.DataRepositorySell
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.utils.dataToUiOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(private val dataRepository: DataRepositorySell) : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    init {
        MainMessagePipe.uiEvent.subscribe {
            if (it is UiEvent.LogOut){
                disposable.clear()
            }
        }.addTo(disposable)
    }

    val orders: MutableLiveData<List<UiState.Order>> by lazy {
        MutableLiveData<List<UiState.Order>>().also { loadNextOrders() }
    }

    private fun loadNextOrders() {
        dataRepository.loadNextOrders().subscribe({ listOfOrders ->
            orders.value =  listOfOrders.map { it.dataToUiOrder() }
        }, {
        }).addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }


}