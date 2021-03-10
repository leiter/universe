package com.together.overview

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.together.R
import com.together.base.ProdAdapter
import com.together.base.UiState
import com.together.databinding.ProductViewsFragmentBinding
import com.together.utils.viewLifecycleLazy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductViewsFragment : Fragment(R.layout.product_views_fragment) {

    private val viewBinding: ProductViewsFragmentBinding by viewLifecycleLazy {
        ProductViewsFragmentBinding.bind(requireView())
    }

    private val adapter = ProdAdapter()

    @Inject
    lateinit var viewModel: ProductViewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProvider(this).get(ProductViewsViewModel::class.java)
        with(viewBinding){
            btnFilterOptions.setOnClickListener { showPopup() }
            btnGoBack.setOnClickListener { findNavController()
                .navigate(R.id.action_productViewsFragment_to_createFragment) }
            rvProductView.layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL,false)
            rvProductView.adapter = adapter

        }
        viewModel.productList.observe(viewLifecycleOwner, observeAll)
    }

    private val observeAll = Observer<List<UiState.Article>> { t -> adapter.submitList(t) }

    private fun showPopup() {
        val popupMenu = PopupMenu(requireActivity(), viewBinding.btnFilterOptions)
        popupMenu.menuInflater.inflate(R.menu.menu_product_views, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.btn_buyer_view -> {

                    true
                }
                R.id.btn_not_available -> {

                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }
        popupMenu.show()
    }


}