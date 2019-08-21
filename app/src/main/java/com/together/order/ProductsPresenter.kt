package com.together.order

import androidx.fragment.app.FragmentManager
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.create.CreateFragment

interface ProductView {


    fun giveFragmentManager() : FragmentManager


}

class ProductsPresenter (val view: ProductView) {
    fun startEditProducts() {
        MainMessagePipe.uiEvent.onNext(
            UiEvent.AddFragment(view.giveFragmentManager(),
                CreateFragment(),CreateFragment.TAG))
        MainMessagePipe.uiEvent.onNext(UiEvent.LockDrawer)

    }


}