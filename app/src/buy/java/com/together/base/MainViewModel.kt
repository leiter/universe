package com.together.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.utils.*
import io.reactivex.disposables.CompositeDisposable
import java.util.*

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


class MainViewModel(private val dataRepository: DataRepository = DataRepositoryImpl()) :
    ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    private val productData: MutableLiveData<MutableList<UiState.Article>> =
        MutableLiveData(emptyList<UiState.Article>().toMutableList())

    val productList: LiveData<MutableList<UiState.Article>>
        get() {
            return productData
        }

    var buyerProfile = UiState.BuyerProfile()

    val uiTasks: LiveData<out UiState> = MutableLiveData()

    private val order = Result.Order()

    lateinit var days: Array<Date>

    lateinit var sellerProfile: UiState.SellerProfile

    var marketIndex: Int = 0
    set(value) {
        field = value
        if (::sellerProfile.isInitialized){
            marketText.value = sellerProfile.marketList[value].name
        }
    }

    var dateIndex: Int = 0
        set(value) {
            field = value
            if (::days.isInitialized){
                dayText.value = days[value].toAppointmentTime()
            }
        }

    val marketText: MutableLiveData<String> = MutableLiveData()
    val dayText: MutableLiveData<String> = MutableLiveData()


    init {
        disposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is Result.LoggedOut -> {
                    loggedState.value = UiState.LOGGEDOUT
                    FireBaseAuth.loginAnonymously()
                }

                is Result.LoggedIn -> {
                    loggedState.value = UiState.BASE_AUTH
                    setupDataStreams()
                }

                is Result.NewImageCreated -> {
                    if (newProduct.value == null)
                        newProduct.value = UiState.NewProductImage(it.uri!!)
                    else newProduct.value = UiState.NewProductImage(it.uri!!)
                }
                is Result.ImageLoaded -> {
                    imageLoadingProgress.value =
                        UiState.LoadingProgress(it.progressId, it.show)
                }
            }
        })
    }

    private fun setupDataStreams() {
        disposable.add(dataRepository.setupProviderConnection()
            .subscribe({ sellerProfile = it.dataToUiSeller() },
                { it.printStackTrace() })
        )
        disposable.add(
            dataRepository.setupProductConnection()
                .subscribe({
                    val e = it.dataArticleToUi()
                    productData.value?.addItem(e, productData)
                    if (productData.value?.size == 1) {
                        presentedProduct.value = productData.value!![0]
                    }
                }, { it.printStackTrace() },
                    { Log.e("Rx", "Complete called."); })

        )

    }

    fun resetAmountCount(id: String){
        productData.value?.first { it._id ==id }?.pieceCounter =0
    }

    fun sendOrder() {
        if (loggedState.value == UiState.LOGGEDOUT) {
            loggedState.value = UiState.LOGIN_REQUIRED
            return
        }
        order.sellerId = sellerProfile._id
        order.createdDate = System.currentTimeMillis()
        order.articles = basket.value?.map {
            it.toOrderedItem()
        }!!
        dataRepository.sendOrder(order)
    }

    val imageLoadingProgress = MutableLiveData<UiState.LoadingProgress>().also {
        it.value = UiState.LoadingProgress(-1, false)
    }
    var smsMessageText = ""

    val blockingLoaderState: MutableLiveData<UiEvent> by lazy {
        MutableLiveData<UiEvent>().also { it.value = UiEvent.LoadingDone(-1) }
    }

    val loggedState: MutableLiveData<UiState> by lazy {
        MutableLiveData<UiState>().also {
            it.value = FireBaseAuth.isLoggedIn()
        }
    }

    val presentedProduct: MutableLiveData<UiState.Article> = MutableLiveData()

    private val newProduct: MutableLiveData<UiState.NewProductImage> = MutableLiveData()

    val basket: MutableLiveData<MutableList<UiState.Article>> by lazy {
        MutableLiveData<MutableList<UiState.Article>>().also {
            it.value = mutableListOf()
        }
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    fun setTimeDateForOrder(market: UiState.Market, date: Date) {
        order.pickUpDate = date.time
        order.marketId = market._id
    }
}

