package com.together.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.base.UiEvent.Companion.CLEAR_ACCOUNT
import com.together.base.UiEvent.Companion.LOAD_OLD_ORDERS
import com.together.base.UiEvent.Companion.SEND_ORDER
import com.together.base.UiEvent.Companion.SEND_ORDER_FAILED
import com.together.base.UiEvent.Companion.SEND_ORDER_UPDATED
import com.together.base.UiEvent.Companion.UPLOAD_PROFILE
import com.together.repository.AlreadyPlaceOrder
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getSingle
import com.together.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.lang.IndexOutOfBoundsException
import java.util.*
import kotlin.collections.ArrayList


class MainViewModel(private val dataRepository: DataRepository = DataRepositoryImpl()) :
    ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    private val productData: MutableLiveData<MutableList<UiState.Article>> =
        MutableLiveData(emptyList<UiState.Article>().toMutableList())

    val productList: LiveData<MutableList<UiState.Article>>
        get() {
            return productData
        }

    lateinit var buyerProfile: UiState.BuyerProfile

    var oldOrders: MutableLiveData<List<UiState.Order>> = MutableLiveData()

    var order = UiState.Order()

    var updateOrder = false

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
                    loggedState.value = UiState.LoginRequired()
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
            .subscribe({ sellerProfile = it.dataToUiSeller()
                       days = getDays(sellerProfile.marketList[0])
                       },
                { it.printStackTrace() }).addTo(disposable)

        dataRepository.setupProductConnection()
            .subscribe({
                val e = it.dataArticleToUi()
                productData.addItem(e)
                if (productData.value?.size == 1) {
                    presentedProduct.value = productData.value!![0]
                }
            }, { it.printStackTrace() },
                { Log.d("MainViewModel", "Rx Complete called."); }).addTo(disposable)

        dataRepository.loadBuyerProfile().subscribe({ buyer ->
            buyerProfile = buyer.dataToUiOrder()
            setMarketAndTime()
            }, {
            it.printStackTrace()
        }).addTo(disposable)

    }

    private fun setMarketAndTime() {
        var market: UiState.Market? = null
        if (buyerProfile.defaultMarket != "") {
            sellerProfile.marketList.find { buyerProfile.defaultMarket == it._id }?.let {
                market = it
                val n = sellerProfile.marketList.indexOf(it)
                if (n > -1) marketIndex = n
            }
        }
        if (buyerProfile.defaultTime != "" && market != null) {
            val calendar = Calendar.getInstance()
            val time = buyerProfile.defaultTime.split(":")
            calendar.set(Calendar.HOUR_OF_DAY, time[0].toInt() )
            calendar.set(Calendar.MINUTE, time[1].toInt() )
            days = getDays(market = market!!, calendar.time)
        }
    }

    fun resetAmountCount(id: String) {
        productData.value?.first { it._id == id }?.pieceCounter = 0
    }

    fun uploadBuyerProfile() {
        blockingLoaderState.value = UiEvent.Loading(UPLOAD_PROFILE)
        buyerProfile = buyerProfile.copy(displayName = buyerProfile.displayName)
        dataRepository.saveBuyerProfile(buyerProfile.uiBuyerProfileToData()).subscribe(
            {
                setMarketAndTime()
                blockingLoaderState.value = UiEvent.LoadingDone(UPLOAD_PROFILE)

            }, {
                blockingLoaderState.value = UiEvent.LoadingDone(UPLOAD_PROFILE)
            }
        ).addTo(disposable)
    }

    fun sendOrder() {
        blockingLoaderState.value = UiEvent.Loading(SEND_ORDER)
        order.createdDate = System.currentTimeMillis()
        val sendOrder: Result.Order = order.uiBuyerProfileToData()
        sendOrder.pickUpDate = days[dateIndex].time
        sendOrder.sellerId = sellerProfile._id
        sendOrder.articles = basket.value?.map { it.toOrderedItem() }!!
        dataRepository.sendOrder(sendOrder, updateOrder).subscribe({
            if (updateOrder) updateOrder = false
            blockingLoaderState.value = UiEvent.LoadingDone(SEND_ORDER)
        }, {
            if (it is AlreadyPlaceOrder) {
                loadExistingOrder(sendOrder)
            } else blockingLoaderState.value = UiEvent.LoadingDone(SEND_ORDER_FAILED)
        }).addTo(disposable)
    }

    private fun loadExistingOrder(sendOrder: Result.Order) {
        dataRepository.loadExistingOrder(sendOrder.pickUpDate.toOrderId()).map { loadedOrder ->
            order = loadedOrder.dataToUiOrder()
            val currentBasket = basket.value?.toMutableList()
            val loadedBasket =
                createBasketUDate(productList.value!!.toList(), loadedOrder.dataToUiOrder())
            val result: ArrayList<UiState.Article> = ArrayList()
            result.addAll(currentBasket!!.toTypedArray())
            result.addAll(loadedBasket.toTypedArray())
            result.sortBy { it.productName }
            val f = result.filterIndexed { index, article ->
                if (index < result.size - 2 && result.size > 1) {
                    val next = result[index + 1]
                    return@filterIndexed !(article.productName == next.productName &&
                            article.amount == next.amount)
                } else {
                    return@filterIndexed try {
                        !(article.productName == result[index - 1].productName &&
                                article.amount == result[index - 1].amount)
                    } catch (e: IndexOutOfBoundsException) {
                        true
                    }
                }
            }
            basket.value = f.toMutableList()
        }.subscribe({
            updateOrder = true
            blockingLoaderState.value = UiEvent.LoadingDone(SEND_ORDER_UPDATED)
        }, {
            blockingLoaderState.value = UiEvent.LoadingDone(SEND_ORDER_FAILED)
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
        MutableLiveData<UiState>().also { it.value = FireBaseAuth.isLoggedIn() }
    }

    val presentedProduct: MutableLiveData<UiState.Article> = MutableLiveData()

    private val newProduct: MutableLiveData<UiState.NewProductImage> = MutableLiveData()

    val basket: MutableLiveData<MutableList<UiState.Article>> by lazy {
        MutableLiveData<MutableList<UiState.Article>>().also {
            it.value = mutableListOf()
        }
    }

    fun clearAccount() {
        blockingLoaderState.value = UiEvent.Loading(CLEAR_ACCOUNT)
        dataRepository.clearUserData().flatMap {
            FireBaseAuth.deleteAccount().getSingle()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                basket.value = mutableListOf()
                blockingLoaderState.value = UiEvent.LoadingDone(CLEAR_ACCOUNT)
            }, {
                blockingLoaderState.value = UiEvent.LoadingDone(CLEAR_ACCOUNT)

            }).addTo(disposable)
    }

    fun loadOrders() {
        blockingLoaderState.value = UiEvent.Loading(LOAD_OLD_ORDERS)

        dataRepository.loadOrders().subscribe({ listOfOrders ->
            val newStuff = listOfOrders.map { it.dataToUiOrder() }
            oldOrders.value = newStuff.reversed()
            blockingLoaderState.value = UiEvent.LoadingDone(LOAD_OLD_ORDERS)
        }, {
            it
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

