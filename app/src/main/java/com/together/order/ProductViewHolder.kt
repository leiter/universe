package com.together.order

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.together.base.UiState
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_product.*

class ArticleViewHolder(override val containerView: View?, private val click: ProductAdapter.ItemClicked)

    : RecyclerView.ViewHolder(containerView!!), LayoutContainer {

    fun bindItem(position: Int, item: UiState.Article) {
        product_name.text = item.productName
        val price = item.pricePerUnit+ " €/" + item.unit
        product_price.text = price
        containerView?.setOnClickListener {
            click.clicked(item)
        }
    }

}