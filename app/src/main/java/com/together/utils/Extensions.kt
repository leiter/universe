package com.together.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


fun View.hideIme() {
    val inputMethod =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.hideSoftInputFromWindow(windowToken, 0)
    clearFocus()
}