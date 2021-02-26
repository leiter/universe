package com.together.base


import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable


abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    val viewModel: MainViewModel by activityViewModels()

    val disposable = CompositeDisposable()

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

}
