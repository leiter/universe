package com.together.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.base.UiState
import com.together.base.UiState.Companion.ADDED
import com.together.base.UiState.Companion.CHANGED
import com.together.base.UiState.Companion.REMOVED
import com.together.databinding.ItemProductBinding


class ProductAdapter(private val click: ItemClicked,
                     var data: MutableList<UiState.Article> = mutableListOf())
    : RecyclerView.Adapter<ArticleViewHolder>() {

    interface ItemClicked {
        fun clicked(item: UiState.Article)
    }

    fun setFilteredList(list:MutableList<UiState.Article>) {
        data = list
        notifyDataSetChanged()
    }


    fun addItem(item: UiState.Article) {
        val index = data.indexOf(item)
        when (item._mode) {
            ADDED -> data.add(item)
            REMOVED -> data.removeAt(data.indexOf(data.first { it._id == item._id }))
            CHANGED -> {
                if(index == -1) {
                    val i = data.indexOf(data.first { it._id == item._id })
                    data.removeAt(i)
                    data.add(i, item)
                } else {
                    data.removeAt(index)
                    data.add(index, item)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArticleViewHolder(binding, click)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bindItem(data[position])
    }


}
