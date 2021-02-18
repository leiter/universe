package com.together.orders

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.base.DataRepositorySell
import com.together.base.UiState
import com.together.utils.dataToUiOrder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo


class OrdersViewModel @ViewModelInject constructor(private val dataRepository: DataRepositorySell) : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    val orders: MutableLiveData<List<UiState.Order>> by lazy {
        MutableLiveData<List<UiState.Order>>().also {
            loadNextOrders()
        }
    }


    fun loadNextOrders() {
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