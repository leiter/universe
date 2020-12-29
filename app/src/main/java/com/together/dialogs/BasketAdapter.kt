package com.together.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.base.UiState

class BasketAdapter(var data: MutableList<UiState.Article>,
                    private val clickToDelete: (product: UiState.Article) -> Unit) : RecyclerView.Adapter<BasketItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasketItemViewHolder {
        return BasketItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_basket, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: BasketItemViewHolder, position: Int) {
        holder.bindItem(data[position], clickToDelete)
    }
}


