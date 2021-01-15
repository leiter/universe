package com.together.dialogs

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.together.base.UiState
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_basket.*

class BasketItemViewHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView!!),
    LayoutContainer {

    fun bindItem(item: UiState.Article, clickToDelete: (product: UiState.Article) -> Unit) {
        edit_order.setOnClickListener { clickToDelete(item) }
        val amount = item.amountDisplay
        et_product_amount.text = amount
        product_name.text = item.productName
        product_price.text = item.priceDisplay
    }
}