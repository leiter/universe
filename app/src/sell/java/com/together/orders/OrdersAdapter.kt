package com.together.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.base.UiState
import com.together.databinding.ItemNextOrderBinding

class OrdersAdapter(var data: MutableList<UiState.Order> = mutableListOf()) : RecyclerView.Adapter<OrderItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemNextOrderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bindItem(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class OrderItemViewHolder(private val binding: ItemNextOrderBinding) : RecyclerView.ViewHolder(binding.root){

    fun bindItem(item: UiState.Order){

    }
}