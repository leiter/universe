package com.together.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.base.UiEvent.Companion.LOAD_OLD_ORDERS
import com.together.base.UiEvent.Companion.SEND_ORDER
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getSingle
import com.together.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.*

fun MutableLiveData<MutableList<UiState.Article>>.addItem(
    item: UiState.Article,
) {

    val index = value!!.indexOf(item)
    when (item._mode) {
        UiState.ADDED -> value!!.add(item)
        UiState.REMOVED -> value!!.removeAt(value!!.indexOf(value!!.first { it._id == item._id }))
        UiState.CHANGED -> {
            if (index == -1) {
                val i = value!!.indexOf(value!!.first { it._id == item._id })
                value!!.removeAt(i)
                value!!.add(i, item)
            } else {
                value!!.removeAt(index)
                value!!.add(index, item)
            }
        }
    }
    value = value!!.toMutableList()
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

    private var buyerProfile = UiState.BuyerProfile()

    var oldOrders: MutableLiveData<List<UiState.Order>> = MutableLiveData()

    var order = UiState.Order()

    lateinit var days: Array<Date>

    lateinit var sellerProfile: UiState.SellerProfile

    var marketIndex: Int = 0
        set(value) {
            field = value
            if (::sellerProfile.isInitialized) {
                marketText.value = sellerProfile.marketList[value].name
            }
        }

    var dateIndex: Int = 0
        set(value) {
            field = value
            if (::days.isInitialized) {
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
                }

                is Result.LoggedIn -> {
                    val user = UiState.BuyerProfile(
                        isAnonymous = it.currentUser.isAnonymous
                    )
                    buyerProfile = user
                    loggedState.value = UiState.BaseAuth(user)
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

        dataRepository.setupProviderConnection()
            .subscribe({ sellerProfile = it.dataToUiSeller() },
                { it.printStackTrace() }).addTo(disposable)

        dataRepository.setupProductConnection()
            .subscribe({
                val e = it.dataArticleToUi()
                productData.addItem(e)
                if (productData.value?.size == 1) {
                    presentedProduct.value = productData.value!![0]
                }
            }, { it.printStackTrace() },
                { Log.e("Rx", "Complete called."); }).addTo(disposable)
    }

    fun resetAmountCount(id: String) {
        productData.value?.first { it._id == id }?.pieceCounter = 0
    }

    fun sendOrder() {
        blockingLoaderState.value = UiEvent.Loading(SEND_ORDER)
        order.createdDate = System.currentTimeMillis()
        val sendOrder: Result.Order = order.uiOrderToData()
        sendOrder.sellerId = sellerProfile._id
        sendOrder.articles = basket.value?.map { it.toOrderedItem() }!!
        dataRepository.sendOrder(sendOrder).subscribe({
            blockingLoaderState.value = UiEvent.LoadingDone(SEND_ORDER)
        }, {
            blockingLoaderState.value = UiEvent.LoadingDone(SEND_ORDER)
        }).addTo(disposable)
    }

    val imageLoadingProgress = MutableLiveData<UiState.LoadingProgress>().also {
        it.value = UiState.LoadingProgress(-1, false)
    }
    var smsMessageText = ""

    val blockingLoaderState: MutableLiveData<UiEvent> by lazy {
        MutableLiveData<UiEvent>().also { it.value = UiEvent.LoadingNeutral }
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

    fun clearAccount() {
        dataRepository.clearUserData().flatMap {
            FireBaseAuth.deleteAccount().getSingle()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
//                    loggedState.value = UiState.LOGGEDOUT
            }, {

            }).addTo(disposable)
    }

    fun loadOrders() {
        blockingLoaderState.value = UiEvent.Loading(LOAD_OLD_ORDERS)

        dataRepository.loadOrders().subscribe({ listOfOrders ->
            val newStuff = listOfOrders.map {
                it.dataToUiOrder()
            }
            oldOrders.value = newStuff
            blockingLoaderState.value = UiEvent.LoadingDone(LOAD_OLD_ORDERS)

        }, {

            blockingLoaderState.value = UiEvent.LoadingDone(LOAD_OLD_ORDERS)
        }).addTo(disposable)
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

