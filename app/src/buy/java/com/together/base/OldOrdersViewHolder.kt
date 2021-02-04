package com.together.base

import androidx.recyclerview.widget.RecyclerView
import com.together.databinding.ItemOldOrderBinding
import com.together.utils.toOrderTime

class OldOrdersViewHolder (private val binding: ItemOldOrderBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(order: UiState.Order, clickToOpen: (product: UiState.Order) -> Unit) {
        binding.tvTitle.text = order.pickUpDate.toOrderTime()
        binding.tvProducts.text = order.createProductList()
        binding.tvProductCount.text = order.createProductCount()
        binding.root.setOnClickListener {
            clickToOpen(order)
        }
    }
}