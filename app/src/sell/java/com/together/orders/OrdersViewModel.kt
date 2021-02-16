package com.together.orders

import androidx.lifecycle.ViewModel
import com.together.data.DataRepositorySell
import com.together.data.DataRepositorySellImpl

class OrdersViewModel constructor(val dataRepository: DataRepositorySell = DataRepositorySellImpl()) : ViewModel() {


}