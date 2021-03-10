package com.together.overview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.base.DataRepositorySell
import com.together.base.UiState
import com.together.base.addItem
import com.together.utils.dataArticleToUi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

@HiltViewModel
class ProductViewsViewModel @Inject constructor(private val dataRepository: DataRepositorySell) : ViewModel() {


    private var disposable: CompositeDisposable = CompositeDisposable()

    private val productData: MutableLiveData<MutableList<UiState.Article>> by lazy {
        dataRepository.setupProductConnection()
            .subscribe({
                val e = it.dataArticleToUi()
                productData.value?.addItem(e, productData)
            }, { it.printStackTrace() },
                { Log.e("Rx", "Complete called."); }).addTo(disposable)
        MutableLiveData(emptyList<UiState.Article>().toMutableList())
    }

    val productList: LiveData<MutableList<UiState.Article>>
        get() {
            return productData
        }

}