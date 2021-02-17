package com.together.orders

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.together.R
import com.together.base.UiState
import com.together.databinding.ItemOrderedProductBinding

class OrderedProductAdapter(context: Context,
                            private val data: List<UiState.OrderedProduct>)
    : ArrayAdapter<UiState.OrderedProduct>(context,R.layout.item_ordered_product,data) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemOrderedProductBinding.inflate(LayoutInflater.from(context), parent,false)
        val product = data[position]
        with(binding){
            tvAmount.text = product.amount
            tvProductName.text = product.productName
        }
        return binding.root
    }

}
