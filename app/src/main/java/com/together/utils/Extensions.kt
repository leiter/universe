package com.together.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telecom.ConnectionService
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


fun View.hideIme() {
    val inputMethod =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.hideSoftInputFromWindow(windowToken, 0)
    clearFocus()
}

fun View.showIme() {
    this.requestFocus()
    val inputMethod = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
}

fun Activity.setDrawable(resId: Int, imageView: AppCompatImageButton){
    val d = ResourcesCompat.getDrawable(resources, resId, this.theme)
    imageView.setImageDrawable(d)
}

fun Context.hasInternet(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun Context.loadImage(imageView: ImageView, url: String){
    Picasso.with(this)
        .load(url)
        .networkPolicy(NetworkPolicy.OFFLINE)
        .into(imageView, object : Callback {
            override fun onSuccess() {
                MainMessagePipe.mainThreadMessage.onNext(
                    Result.ImageLoaded(R.id.pr_load_image_progress, false))
            }

            override fun onError() {
                Picasso.with(applicationContext)
                    .load(url)
                    .error(R.drawable.obst_1)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {
                            MainMessagePipe.mainThreadMessage.onNext(
                                Result.ImageLoaded(R.id.pr_load_image_progress,false))
                        }

                        override fun onError() {
                            MainMessagePipe.mainThreadMessage.onNext(
                                Result.ImageLoaded(R.id.pr_load_image_progress,false))
                        }
                    })
            }
        })
}

fun MutableLiveData<MutableList<UiState.Article>>.addItem(
    item: UiState.Article,
) {

    val index = value!!.indexOfFirst {item._id == it._id}
    when (item._mode) {
        UiState.ADDED -> if(index==-1) value!!.add(item)
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
