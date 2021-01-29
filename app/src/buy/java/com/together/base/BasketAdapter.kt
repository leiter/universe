package com.together.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.databinding.ItemBasketBinding
import com.together.dialogs.BasketItemViewHolder

class BasketAdapter(var data: MutableList<UiState.Article>,
                    private val clickToDelete: (product: UiState.Article) -> Unit)
    : RecyclerView.Adapter<BasketItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasketItemViewHolder {
        val binding = ItemBasketBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BasketItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: BasketItemViewHolder, position: Int) {
        holder.bindItem(data[position], clickToDelete)
    }
}


