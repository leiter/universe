package com.together.base


import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment : Fragment() {

    val viewModel: MainViewModel by viewModels()

    val disposable = CompositeDisposable()


    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

}
