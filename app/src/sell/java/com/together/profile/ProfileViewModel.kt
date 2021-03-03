package com.together.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.base.DataRepositorySell
import com.together.base.DataRepositorySellImpl
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.utils.dataToUiSeller
import com.together.utils.uiSellerToData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class ProfileViewModel(private val dataRepository: DataRepositorySell = DataRepositorySellImpl()) : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    var currentMarket = UiState.Market()
    fun setCurrentMarket(index : Int = -1 ){
        currentMarket = if (index>-1)
            profileLive.value!!.marketList[index] else UiState.Market()
    }

    fun fillInMarket() {
        val profile = profileLive.value!!
        val items = profile.marketList
        val s = items.indexOfFirst { it.id == currentMarket.id }
        if (s > -1) {
            items.removeAt(s)
            items.add(s, currentMarket)
        } else items.add(currentMarket)

        val newProfile = profile.copy(marketList = items)
        profileLive.value = newProfile
        this.profile = newProfile.copy()
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
        p.markets.forEach {
            it.begin = it.begin.replace(" Uhr" ,"")
            it.end = it.end.replace(" Uhr" ,"")
        }
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