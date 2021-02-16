package com.together.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.data.DataRepositorySell
import com.together.data.DataRepositorySellImpl
import com.together.data.UiEvent
import com.together.data.UiState
import com.together.utils.dataToUiSeller
import com.together.utils.uiMarketToData
import com.together.utils.uiSellerToData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class ProfileViewModel(private val dataRepository: DataRepositorySell = DataRepositorySellImpl()) : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    val markets: MutableLiveData<MutableList<UiState.Market>> by lazy {
        MutableLiveData<MutableList<UiState.Market>>().also {
            it.value = mutableListOf()
        }
    }

    val profileLive: MutableLiveData<UiState.SellerProfile> by lazy {
        MutableLiveData<UiState.SellerProfile>().also {
            it.value = UiState.SellerProfile()
            loadProfile()

        }
    }

    private fun loadProfile(){
        dataRepository.loadSellerProfile().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    val seller = it.dataToUiSeller()
                    profileLive.value = seller
                    profile = seller
                    markets.value = seller.marketList

                },{
                    it.printStackTrace()
                }
            ).addTo(disposable)
    }

    var profile: UiState.SellerProfile = UiState.SellerProfile()

    val blockingLoaderState: MutableLiveData<UiEvent> by lazy {
        MutableLiveData<UiEvent>().also { it.value = UiEvent.LoadingDone(UiEvent.UNDEFINED) }
    }

    fun uploadSellerProfile() {
        blockingLoaderState.value = UiEvent.Loading(0)
        val p = profile.uiSellerToData()
        p.markets = markets.value!!.map { it.uiMarketToData() }.toMutableList()
        dataRepository.uploadSellerProfile(p).subscribe({ success ->
            if (success) {
                blockingLoaderState.value = UiEvent.LoadingDone(UiEvent.UPLOAD_PRODUCT)
                blockingLoaderState.value = UiEvent.ShowCreateFragment
            } else {
                blockingLoaderState.value = UiEvent.LoadingDone(UiEvent.UPLOAD_PRODUCT)
            }
        }, {
            blockingLoaderState.value = UiEvent.LoadingDone(UiEvent.UPLOAD_PRODUCT)
        }).addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

}