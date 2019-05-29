package com.together.order.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.app.UiState
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.product_item.*

class ArticleAdapter(val data: MutableList<UiState.Article>)

    : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {


    fun update(newData: MutableList<UiState.Article>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
       return ArticleViewHolder(LayoutInflater.from(parent.context)
           .inflate(R.layout.product_item ,parent,false))
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
         holder.bindItem(position)
    }

    inner class ArticleViewHolder(override val containerView: View?)

        : RecyclerView.ViewHolder(containerView!!), LayoutContainer{

        fun bindItem(position: Int) {
            product_name.text = data[position].productName
        }

    }
}