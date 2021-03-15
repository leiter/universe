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
import com.together.utils.viewLifecycleLazy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class ProductViewsFragment : Fragment(R.layout.product_views_fragment) {

    companion object {
        private const val AVAILABLE_ITEMS = 0
        private const val NOT_AVAILABLE_ITEMS = 1
        const val KEY_PRODUCT_LIST = "product_list"
    }

    private val disposable = CompositeDisposable()

    private val viewBinding: ProductViewsFragmentBinding by viewLifecycleLazy {
        ProductViewsFragmentBinding.bind(requireView())
    }

    private lateinit var data : UiState.ProductList
//    by lazy {
//        arguments?.getParcelable(KEY_PRODUCT_LIST) as? UiState.ProductList ?: UiState.ProductList()
//    }

    private val adapter = ProdAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding){
            btnFilterOptions.setOnClickListener { showPopup() }
            btnGoBack.setOnClickListener { findNavController()
                .navigate(R.id.action_productViewsFragment_to_createFragment) }
            rvProductView.layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL,false)
            rvProductView.adapter = adapter
//            setListState(AVAILABLE_ITEMS)
        }

        MainMessagePipe.transferCache.subscribe {
            if (it is UiState.ProductList) {
                data = it
                setListState(AVAILABLE_ITEMS)
            }
        }.addTo(disposable)
    }

    private fun setListState(filterMode: Int)  {
        var result: List<UiState.Article> = ArrayList()
        when (filterMode){
            AVAILABLE_ITEMS -> {
                result = data.list.filter { it.available }
                viewBinding.tvMenuTitle.setText(R.string.btn_show_available_products)
            }
            NOT_AVAILABLE_ITEMS -> {
                result = data.list.filter { !it.available }
                viewBinding.tvMenuTitle.setText(R.string.btn_show_not_available_products)
            }
        }
        adapter.submitList(result)
    }

    private fun showPopup() {
        val popupMenu = PopupMenu(requireActivity(), viewBinding.btnFilterOptions)
        popupMenu.menuInflater.inflate(R.menu.menu_product_views, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.btn_buyer_view -> {
                    setListState(AVAILABLE_ITEMS)
                    true
                }
                R.id.btn_not_available -> {
                    setListState(NOT_AVAILABLE_ITEMS)
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }
        popupMenu.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }
}