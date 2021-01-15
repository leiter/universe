package com.together.utils

import com.together.base.UiState
import com.together.repository.Result
import kotlin.reflect.full.memberProperties

fun Result.Article.dataArticleToUi() : UiState.Article {
    return UiState.Article(
        _id = this.id,
        productName = this.productName,
        remoteImageUrl = this.imageUrl,
        unit = this.unit,
        pricePerUnit = "%.2f€".format(this.price).replace(".",","),
        priceDigit = this.price,
        _mode = this.mode,
        available = this.available,
        category = this.category,
        detailInfo = this.detailInfo,
        searchTerms = "${this.searchTerms},${this.category}",
        weightPerPiece = this.weighPerPiece,
        productId = this.productId
    )
}

fun Result.SellerProfile.dataSellerToUi() : UiState.SellerProfile {
    return UiState.SellerProfile(
        _id = this.id,
        displayName = this.displayName,
        firstName = this.firstName,
        lastName = this.lastName,
        street = this.street,
        houseNumber = this.houseNumber,
        city = this.city,
        zipCode = this.zipcode,
        _telephoneNumber = this.telephoneNumber,
        _knownClientIds = this.knownClientIds,
        marketList = this.markets.map { it.dataMarketToUi() }.toMutableList()
    )
}


fun Result.Market.dataMarketToUi() : UiState.Market {
    return UiState.Market(
        _id = this.id,
        marketId = this.id,
        name = this.name,
        street = this.street,
        houseNumber = this.houseNumber,
        city = this.city,
        zipCode = this.zipcode,
        dayOfWeek = this.dayOfWeek,
        begin = this.begin,
        end = this.end,
        dayIndicator = this.dayIndex
    )
}






inline fun <reified T : UiState> errorActions(profile: T, action: () -> Unit)  : Boolean {
    val toBeChecked =
        T::class.memberProperties.filter { !it.name.startsWith("_") }
    toBeChecked.forEach { prop ->
        val p = prop.get(profile) as String
        if(p.isEmpty()) {
            action()
            return false
        }
    }
    return true
}