package com.together.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import io.reactivex.disposables.CompositeDisposable


class MainViewModel : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    init {
        // wire DataSource to UiState

        disposable.add(MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {

                is Result.LoggedOut ->
                    loggedState.value = UiState.LOGGEDOUT

                is Result.LoggedIn ->
                    loggedState.value = UiState.BASE_AUTH

                is Result.NewImageCreated -> {
                    if (newProduct.value == null)
                        newProduct.value = UiState.NewProductImage(it.uri!!)
                    newProduct.value = UiState.NewProductImage(it.uri!!)
                }

//                is Result.SellerProfile -> {
//                    profile = UiState.SellerProfile(
//                        id = it.id,
//                        houseNumber = it.houseNumber,
//                        street = it.street,
//                        zipcode = it.zipcode,
//                        city = it.city,
//                        lastName = it.lastName,
//                        firstName = it.firstName,
//                        displayName = it.displayName
//
//
//                    )
//                }
            }
        }


        )
    }

    val loggedState: MutableLiveData<UiState> by lazy {
        MutableLiveData<UiState>().also { it.value = FireBaseAuth.isLoggedIn() }
    }

    val profile : UiState.SellerProfile = UiState.SellerProfile()

    val presentedProduct: MutableLiveData<UiState.Article> = MutableLiveData()

    val editProduct: MutableLiveData<UiState.Article> by lazy {
        MutableLiveData<UiState.Article>().also {
            it.value = UiState.Article()
        }
    }

    val newProduct: MutableLiveData<UiState.NewProductImage> = MutableLiveData()

    val markets: MutableLiveData<MutableList<UiState.Market>> by lazy {
        MutableLiveData<MutableList<UiState.Market>>().also {
            it.value = mutableListOf()
        }
    }

    val basket: MutableLiveData<MutableList<UiState.Article>> by lazy {
        MutableLiveData<MutableList<UiState.Article>>().also {
            it.value = mutableListOf()
        }
    }








    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }



}

