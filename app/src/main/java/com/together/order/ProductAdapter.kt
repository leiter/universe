package com.together.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.base.UiState
import com.together.base.UiState.Companion.ADDED
import com.together.base.UiState.Companion.CHANGED
import com.together.base.UiState.Companion.REMOVED


class ProductAdapter(private val click: ItemClicked,
                     var data: MutableList<UiState.Article> = mutableListOf()
)

    : RecyclerView.Adapter<ArticleViewHolder>() {

    interface ItemClicked {
        fun clicked(item: UiState.Article)
    }


    fun showAll(showAll:Boolean){

    }

    fun addItem(item: UiState.Article) {
        val index = data.indexOf(item)
        when (item.mode) {
            ADDED -> data.add(item)
            REMOVED -> data.removeAt(index)
            CHANGED -> {
                data.removeAt(index)
                data.add(index, item)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product, parent, false), click
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bindItem(position, data[position])
    }


//    private fun findItemIndex(item: UiState.Article): Int {
//        return data.indexOf(item)
//    }
}
