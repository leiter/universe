package com.together.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    @Inject
    lateinit var viewModel: MainViewModel //by activityViewModels()

    val disposable = CompositeDisposable()

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

}
