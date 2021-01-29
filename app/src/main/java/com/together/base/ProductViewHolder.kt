package com.together.base

import android.graphics.Typeface
import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import com.together.databinding.ItemProductBinding


class ArticleViewHolder(
    private val binding: ItemProductBinding,
    private val click: ProductAdapter.ItemClicked
)
    : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(item: UiState.Article) {
        if(item.isSelected){
            binding.productName.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f)
            binding.productPrice.setTypeface(null,Typeface.BOLD)
        } else {
            binding.productName.setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
            binding.productPrice.setTypeface(null,Typeface.NORMAL)


        }
        binding.productName.text = item.productName
        val price = item.pricePerUnit + "/" + item.unit
        binding.productPrice.text = price
        binding.root.setOnClickListener {
            click.clicked(item)
        }
    }

}