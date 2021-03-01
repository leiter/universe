package com.together.orders

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.together.R
import com.together.databinding.FragmentShowOrdersBinding
import com.together.utils.viewLifecycleLazy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowOrdersFragment : Fragment(R.layout.fragment_show_orders){

    @Inject
    lateinit var viewModel: OrdersViewModel// by viewModels()
    private val adapter: OrdersAdapter = OrdersAdapter()

    private val viewBinding: FragmentShowOrdersBinding by viewLifecycleLazy {
        FragmentShowOrdersBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding){
            btnBack.setOnClickListener{
                findNavController().navigate(R.id.action_showOrdersFragment_to_createFragment)
            }
            rvNewOrders.adapter = adapter
            rvNewOrders.layoutManager = LinearLayoutManager(
                requireActivity(),
                RecyclerView.VERTICAL, false
            )
        }

        viewModel.orders.observe(viewLifecycleOwner, {
            adapter.data = it.toMutableList()
            adapter.notifyDataSetChanged()
        })
    }

}