package com.together.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.create.app.UiState

class ProductAdapter(private val click: ItemClicked,
                     var data: MutableList<UiState.Article> = mutableListOf())

    : RecyclerView.Adapter<ArticleViewHolder>() {

    interface ItemClicked {
        fun clicked(item: UiState.Article)
    }

    fun addItem(item: UiState.Article) {
        data.add(item)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.product_item, parent, false), click
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        if (position == 0) click.clicked(data[position])
        holder.bindItem(position, data[position])
    }

}
