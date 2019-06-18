package com.together.order.main

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.together.app.UiState
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.product_item.*

class ArticleViewHolder(override val containerView: View?, private val click: ProductAdapter.ItemClicked)

    : RecyclerView.ViewHolder(containerView!!), LayoutContainer {

    fun bindItem(position: Int,item: UiState.Article ) {
        product_name.text = item.productName

        containerView?.setOnClickListener {
            click.clicked(item)
        }
    }

}