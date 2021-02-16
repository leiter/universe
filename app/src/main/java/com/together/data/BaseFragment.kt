package com.together.data


import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    val viewModel: MainViewModel by activityViewModels()

    val disposable = CompositeDisposable()

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

}
