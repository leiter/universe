package com.together.utils

import com.together.base.UiState
import com.together.repository.Result
import kotlin.reflect.full.memberProperties

fun Result.Article.dataArticleToUi() : UiState.Article {
    return UiState.Article(
        _id = this.id,
        productName = this.productName,
        productDescription = this.productDescription,
        remoteImageUrl = this.imageUrl,
        unit = this.unit,
        price = "%.2f€".format(this.price).replace(".",","),
        pricePerUnit = "%.2f€".format(this.price).replace(".",","),
        _mode = this.mode,
        available = this.available
    )
}

inline fun <reified T : UiState> errorActions(profile: T, action: () -> Unit)  : Boolean {
    val toBeChecked =
        T::class.memberProperties.filter { !it.name.startsWith("_") }
    toBeChecked.forEach { prop ->
        val p = prop.get(profile) as String
        if(p.isEmpty()) {
            action()
//            MainMessagePipe.uiEvent.onNext(UiEvent.ShowToast(requireContext(), R.string.developer_error_hint))
            return false
        }
    }
    return true
}