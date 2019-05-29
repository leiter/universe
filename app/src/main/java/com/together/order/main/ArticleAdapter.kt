package com.together.order.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.app.UiState
import kotlinx.android.extensions.LayoutContainer

class ArticleAdapter(val data: MutableList<UiState>) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {


    fun update(newData: MutableList<UiState>) {

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {


        LayoutInflater.from(parent.context).inflate(R.layout.product_item ,parent,false)

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
         holder.bindItem(position)
    }


    inner class ArticleViewHolder(override val containerView: View?)
        : RecyclerView.ViewHolder(containerView!!), LayoutContainer{

        fun bindItem(position: Int) {

        }

    }
}