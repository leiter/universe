package com.together.manage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.base.UiState
import com.together.databinding.ItemOldOrderBinding

class OldOrdersAdapter(var data: List<UiState.Order>,
                       private val clickToOpen: (product: UiState.Order) -> Unit)
    : RecyclerView.Adapter<OldOrdersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OldOrdersViewHolder {
        val binding = ItemOldOrderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OldOrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OldOrdersViewHolder, position: Int) {
        holder.bind(data[position], clickToOpen)
    }

    override fun getItemCount(): Int { return data.size }

}

