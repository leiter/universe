package com.together.overview

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.ProdAdapter
import com.together.base.UiState
import com.together.databinding.ProductViewsFragmentBinding
import com.together.utils.getQuantityString
import com.together.utils.showPopup
import com.together.utils.viewLifecycleLazy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class ProductViewsFragment : Fragment(R.layout.product_views_fragment) {

    companion object {
        private const val AVAILABLE_ITEMS = 0
        private const val NOT_AVAILABLE_ITEMS = 1
    }

    private val disposable = CompositeDisposable()

    private val viewBinding: ProductViewsFragmentBinding by viewLifecycleLazy {
        ProductViewsFragmentBinding.bind(requireView())
    }

    private lateinit var data: UiState.ProductList

    private val adapter = ProdAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            btnFilterOptions.setOnClickListener { showPopup() }
            btnGoBack.setOnClickListener {
                findNavController()
                    .navigate(R.id.action_productViewsFragment_to_createFragment)
            }
            rvProductView.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL, false
            )
            rvProductView.adapter = adapter
        }

        MainMessagePipe.transferCache.subscribe {
            if (it is UiState.ProductList) {
                data = it
                setListState(AVAILABLE_ITEMS)
            }
        }.addTo(disposable)
    }

    private fun setListState(filterMode: Int) {
        var result: List<UiState.Article> = ArrayList()
        when (filterMode) {
            AVAILABLE_ITEMS -> {
                result = data.list.filter { it.available }
                val t = requireContext().getQuantityString(R.plurals.tv_show_available_products,
                    result.size,
                    R.string.tv_show_available_products)
                viewBinding.tvMenuTitle.text = t
            }
            NOT_AVAILABLE_ITEMS -> {
                result = data.list.filter { !it.available }
                val t =
                    requireContext().getQuantityString(R.plurals.tv_not_available_products,
                        result.size,
                        R.string.tv_not_available_products)
                viewBinding.tvMenuTitle.text = t
            }
        }
        adapter.submitList(result)
    }

    private val available = { setListState(AVAILABLE_ITEMS) }

    private val notAvailable = { setListState(NOT_AVAILABLE_ITEMS) }

    private fun showPopup() {
        val m = hashMapOf(  R.id.btn_buyer_view to available,
                            R.id.btn_not_available to notAvailable )
        requireContext().showPopup(viewBinding.btnFilterOptions, m, R.menu.menu_product_views)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }
}