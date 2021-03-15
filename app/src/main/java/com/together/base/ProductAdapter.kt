package com.together.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.together.databinding.ItemProductBinding


class ProductAdapter(private val click: ItemClicked,
                     var data: MutableList<UiState.Article> = mutableListOf())
    : RecyclerView.Adapter<ArticleViewHolder>() {

    interface ItemClicked {
        fun clicked(item: UiState.Article)
    }

    private val diffCallback = object : DiffUtil.ItemCallback<UiState.Article>() {
        override fun areItemsTheSame(oldItem: UiState.Article, newItem: UiState.Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UiState.Article, newItem: UiState.Article): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    fun submitList(list: List<UiState.Article>) = differ.submitList(list)


    private val differ = AsyncListDiffer(this, diffCallback)

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bindItem(differ.currentList[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArticleViewHolder(binding, click)
    }






}
