package com.together.basket

import androidx.recyclerview.widget.RecyclerView
import com.together.base.UiState
import com.together.databinding.ItemBasketBinding

class BasketItemViewHolder(private val binding: ItemBasketBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(item: UiState.Article, clickToDelete: (product: UiState.Article) -> Unit) {
        binding.editOrder.setOnClickListener { clickToDelete(item) }
        val amount = item.amountDisplay
        binding.etProductAmount.text = amount
        binding.productName.text = item.productName
        binding.productPrice.text = item.priceDisplay
    }
}