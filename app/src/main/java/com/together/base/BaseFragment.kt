package com.together.base


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment : Fragment() {

    lateinit var viewModel: MainViewModel

    val disposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.dispose()
    }

}
