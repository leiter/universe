package com.together.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.base.UiState
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_basket.*

class BasketAdapter(var data: MutableList<UiState.Article>,
                    val edit: (product: UiState.Article) -> Unit) : RecyclerView.Adapter<BasketItemViewHolder>() {

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
        holder.bindItem(data[position], edit)
    }

}


class BasketItemViewHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView!!), LayoutContainer {

    fun bindItem(item: UiState.Article, edit: (product: UiState.Article) -> Unit) {
        edit_order.setOnClickListener { edit(item) }
        product_amount.text = item.amount
        product_name.text = item.productName
        product_category.text = item.productDescription
        product_price.text = item.price
    }
}