package com.together.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.UiState
import com.together.repository.Result
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.InetAddress

fun View.hideIme() {
    val inputMethod =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.hideSoftInputFromWindow(windowToken, 0)
    clearFocus()
}

fun View.showIme() {
    this.requestFocus()
    val inputMethod = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Activity.setDrawable(resId: Int, imageView: AppCompatImageButton){
    val d = ResourcesCompat.getDrawable(resources, resId, this.theme)
    imageView.setImageDrawable(d)
}

fun Context.getIntIdentity(name: String, type: String) : Int {
    return resources.getIdentifier(name,type,packageName)
}

fun Context.hasInternet(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun isInternetAvailable(): Single<Boolean> {
    return Single.fromCallable{
        try {
            val ipAddr: InetAddress = InetAddress.getByName("google.com")
            //You can replace it with your name
            !ipAddr.equals("")
        } catch (e: Exception) {
            false
        }
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun Context.loadImage(imageView: ImageView, url: String){
    Picasso.with(this)
        .load(url)
        .networkPolicy(NetworkPolicy.OFFLINE)
        .into(imageView, object : Callback {
            override fun onSuccess() {
                imageView.tag = true
                MainMessagePipe.mainThreadMessage.onNext(
                    Result.ImageLoaded(R.id.pr_load_image_progress, false)
                )
            }

            override fun onError() {
                Picasso.with(applicationContext)
                    .load(url)
                    .error(R.drawable.obst_1)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {
                            imageView.tag = true
                            MainMessagePipe.mainThreadMessage.onNext(
                                Result.ImageLoaded(R.id.pr_load_image_progress, false)
                            )
                        }

                        override fun onError() {
                            imageView.tag = false  //fixme
                            MainMessagePipe.mainThreadMessage.onNext(
                                Result.ImageLoaded(R.id.pr_load_image_progress, false)
                            )
                        }
                    })
            }
        })
}

fun MutableLiveData<MutableList<UiState.Article>>.addItem(
    item: UiState.Article,
) {
    val index = value!!.indexOfFirst {item.id == it.id}
    when (item.mode) {
        UiState.ADDED -> if (index == -1 && item.available) value!!.add(item)
        UiState.REMOVED -> value!!.removeAt(value!!.indexOf(value!!.first { it.id == item.id }))
        UiState.CHANGED -> {
            if (index == -1) {
                if (item.available) value!!.add(item)
                else {
                    val i = value!!.indexOf(value!!.first { it.id == item.id })
                    value!!.removeAt(i)
                }
            } else {
                value!!.removeAt(index)
                if (item.available) value!!.add(index, item)
            }
        }
    }
    value = value!!.toMutableList()
}
