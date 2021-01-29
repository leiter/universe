package com.together.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.TestData
import com.together.base.UiEvent.Companion.DELETE_PRODUCT
import com.together.base.UiEvent.Companion.UNDEFINED
import com.together.base.UiEvent.Companion.UPLOAD_PRODUCT
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getSingle
import com.together.utils.dataArticleToUi
import com.together.utils.uiMarketToData
import com.together.utils.uiSellerToData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

fun MutableList<UiState.Article>.addItem(
    item: UiState.Article,
    productData: MutableLiveData<MutableList<UiState.Article>>
) {
    val index = indexOf(item)
    when (item._mode) {
        UiState.ADDED -> add(item)
        UiState.REMOVED -> removeAt(indexOf(first { it._id == item._id }))
        UiState.CHANGED -> {
            if (index == -1) {
                val i = indexOf(first { it._id == item._id })
                removeAt(i)
                add(i, item)
            } else {
                removeAt(index)
                add(index, item)
            }
        }
    }
    productData.value = this.toSet().toMutableList()
}

class MainViewModel(private val dataRepository: DataRepository = DataRepositoryImpl()) : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    init {
        disposable.add( MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is Result.LoggedOut ->
                    loggedState.value = UiState.LOGGEDOUT

                is Result.LoggedIn ->
                    loggedState.value = UiState.BaseAuth(
                        UiState.BuyerProfile(isAnonymous = it.currentUser.isAnonymous)
                    )

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
        MutableLiveData(emptyList<UiState.Article>().toMutableList())}

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

    fun uploadSellerProfile(){
        blockingLoaderState.value = UiEvent.Loading(0)
        val p = profile.uiSellerToData()
        p.markets = markets.value!!.map { it.uiMarketToData() }.toMutableList()
        dataRepository.uploadSellerProfile(p).subscribe({ success ->
            if (success) {
                blockingLoaderState.value = UiEvent.LoadingDone(UPLOAD_PRODUCT)
                blockingLoaderState.value = UiEvent.ShowCreateFragment
            } else {
                blockingLoaderState.value = UiEvent.LoadingDone(UPLOAD_PRODUCT)
//todo message
//                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }, {
            blockingLoaderState.value = UiEvent.LoadingDone(UPLOAD_PRODUCT)
//            Toast.makeText(requireContext(), "EEEEEEEEEEEEeee", Toast.LENGTH_SHORT).show()
        }).addTo(disposable)

    }

    fun deleteProduct(){
        editProduct.value?.let {
            val id = it._id
            if(id.isEmpty()) return  // todo msg
            blockingLoaderState.value = UiEvent.Loading(0)
            dataRepository.deleteProduct(id)
            .subscribe({success ->
                if(success) {
                    blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
                }else {
                    // msg
                    blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
                }
            }, {
                // msg
                blockingLoaderState.value = UiEvent.LoadingDone(DELETE_PRODUCT)
            })
        }
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    fun clear(){
        disposable.clear()
    }
}

