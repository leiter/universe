package com.together.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.together.repository.Result
import com.together.repository.auth.FirebaseAuth
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
        MutableLiveData<UiState>().also {
            it.value = FirebaseAuth.isLoggedIn()
        }
    }

    val profile : UiState.SellerProfile = UiState.SellerProfile()

    val presentedProduct: MutableLiveData<UiState.Article> = MutableLiveData()

    val editProduct: MutableLiveData<UiState.Article> = MutableLiveData()

    val newProduct: MutableLiveData<UiState.NewProductImage> = MutableLiveData()

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }


}

