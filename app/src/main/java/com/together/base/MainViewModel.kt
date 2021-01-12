package com.together.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getCompletable
import com.together.repository.storage.getObservable
import com.together.repository.storage.getSingle
import com.together.utils.dataArticleToUi
import com.together.utils.dataSellerToUi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

fun MutableList<UiState.Article>.addItem(item: UiState.Article,productData: MutableLiveData<MutableList<UiState.Article>>) {
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


class MainViewModel : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    private val productData: MutableLiveData<MutableList<UiState.Article>> =
        MutableLiveData(emptyList<UiState.Article>().toMutableList())

    val productList: LiveData<MutableList<UiState.Article>>
    get() { return productData }

    lateinit var sellerProfile: UiState.SellerProfile



    init {

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
                is Result.ImageLoaded -> {
                    imageLoadingProgress.value = UiState.LoadingProgress(it.progressId, it.show)
                }


            }
        })

        val productNode = Database.providerArticles("FmfwB1HVmMdrVib6dqSXkWauOuP2")
//        disposable.add(productNode.getSingle().subscribe({ Log.e("TTTTT", "For debugging");},{}))
        disposable.add(productNode
            .getObservable<Result.Article>().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                val e = it.dataArticleToUi()
                productData.value?.addItem(e,productData)
                if (productData.value?.size == 1) {
                    presentedProduct.value = productData.value!![0]
                }
            })


        disposable.add(Database.sellerProfile().getSingle<Result.SellerProfile>()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                sellerProfile = it.dataSellerToUi() }
                ,{it.printStackTrace()})
        )

    }

    val imageLoadingProgress = MutableLiveData<UiState.LoadingProgress>().also {
        it.value = UiState.LoadingProgress(-1, false)
    }
    var smsMessageText = ""

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

