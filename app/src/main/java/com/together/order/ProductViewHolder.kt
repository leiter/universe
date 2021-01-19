package com.together.order

import androidx.recyclerview.widget.RecyclerView
import com.together.base.UiState
import com.together.databinding.ItemProductBinding


class ArticleViewHolder(
    private val binding: ItemProductBinding,
    private val click: ProductAdapter.ItemClicked
)
    : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(item: UiState.Article) {
        binding.productName.text = item.productName
        val price = item.pricePerUnit + "/" + item.unit
        binding.productPrice.text = price
        binding.root.setOnClickListener {
            click.clicked(item)
        }
    }

}