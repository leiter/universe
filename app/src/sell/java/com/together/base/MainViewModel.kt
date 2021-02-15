package com.together.base

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.together.base.UiEvent.Companion.DELETE_PRODUCT
import com.together.base.UiEvent.Companion.UNDEFINED
import com.together.base.UiEvent.Companion.UPLOAD_PRODUCT
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.getSingle
import com.together.repository.storage.getTypedSingle
import com.together.utils.*
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.net.URI

fun MutableList<UiState.Article>.addItem(
    item: UiState.Article,
    productData: MutableLiveData<MutableList<UiState.Article>>
) {
    val index = indexOf(item)
    when (item.mode) {
        UiState.ADDED -> add(item)
        UiState.REMOVED -> removeAt(indexOf(first { it.id == item.id }))
        UiState.CHANGED -> {
            if (index == -1) {
                val i = indexOf(first { it.id == item.id })
                removeAt(i)
                add(i, item)
            } else {
                removeAt(index)
                add(index, item)
            }
        }
    }
    productData.value = this.toMutableList()
}

class MainViewModel(private val dataRepository: DataRepository = DataRepositoryImpl()) : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()

    init {
        disposable.add( MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is Result.LoggedOut ->
                    loggedState.value = UiState.LoggedOut

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
            }
        }, {
            blockingLoaderState.value = UiEvent.LoadingDone(UPLOAD_PRODUCT)
        }).addTo(disposable)

    }

    fun deleteProduct(){
        editProduct.value?.let {
            val id = it.id
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

    fun uploadProduct(file: Single<File>) {
        file.subscribeOn(Schedulers.io()).flatMap {
           val uri =  Uri.fromFile(it)
            val dest = FirebaseStorage.getInstance().reference
                .child("images/tt_${System.currentTimeMillis()}_${newProduct.value!!.uri.lastPathSegment}")
                dest.putFile(uri).getSingle()
        }.flatMap {
            it.metadata?.reference?.downloadUrl?.getTypedSingle()
        }.map {
           val newproduct =  editProduct.value!!.uiArticleToData()
            newproduct.imageUrl = it.toString()
            newproduct
        }.map { Database.articles().push().setValue(it) }.subscribe().addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

}

