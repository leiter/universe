package com.together.order

import androidx.fragment.app.FragmentManager

interface ProductView {

    fun giveFragmentManager() : FragmentManager

}

class ProductsPresenter (val view: ProductView) {
    fun startEditProducts() {

    }


}