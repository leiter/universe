package com.together.utils

import com.together.base.UiState
import com.together.repository.Result
import kotlin.reflect.full.memberProperties

fun Result.Article.dataArticleToUi(): UiState.Article {
    return UiState.Article(
        _id = this.id,
        productName = this.productName,
        remoteImageUrl = this.imageUrl,
        unit = this.unit,
        pricePerUnit = "%.2f€".format(this.price).replace(".", ","),
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

fun Result.SellerProfile.dataToUiSeller(): UiState.SellerProfile {
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
        marketList = this.markets.map { it.dataToUiMarket() }.toMutableList()
    )
}

fun UiState.SellerProfile.uiSellerToData(): Result.SellerProfile {
    return Result.SellerProfile(
        id = this._id,
        displayName = this.displayName,
        firstName = this.firstName,
        lastName = this.lastName,
        street = this.street,
        houseNumber = this.houseNumber,
        city = this.city,
        zipcode = this.zipCode,
        telephoneNumber = this._telephoneNumber,
        knownClientIds = this._knownClientIds,
        markets = this.marketList.map { it.uiMarketToData() }.toMutableList()
    )
}


fun Result.Market.dataToUiMarket(): UiState.Market {
    return UiState.Market(
        _id = this.id,
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

fun UiState.Market.uiMarketToData(): Result.Market {
    return Result.Market(
        id = this._id,
        name = this.name,
        street = this.street,
        houseNumber = this.houseNumber,
        city = this.city,
        zipcode = this.zipCode,
        dayOfWeek = this.dayOfWeek,
        begin = this.begin,
        end = this.end,
        dayIndex = this.dayIndicator
    )
}

fun UiState.Article.toOrderedItem(): Result.OrderedProduct {
    return Result.OrderedProduct(
        id = _id,
        productId = productId,
        unit = unit,
        productName = productName,
        price = priceDigit,
        amount = amountDisplay,
        amountCount = amountCount,
        piecesCount = pieces
    )
}

fun Result.Order.dataToUiOrder(): UiState.Order {
    return UiState.Order(
        _id = id,
        _mode = mode,
        buyerProfile = buyerProfile.dataToUiOrder(),
        createdDate = createdDate,
        marketId = marketId,
        pickUpDate = pickUpDate,
        message = message,
        isNotFavourite = isNotFavourite,
        productList = articles.map { it.dataToUiOrderedProduct() },
    )
}
fun UiState.Order.uiBuyerProfileToData(): Result.Order {
    return Result.Order(
        id = _id,
        mode = _mode,
        buyerProfile = buyerProfile.uiBuyerProfileToData(),
        createdDate = createdDate,
        marketId = marketId,
        pickUpDate = pickUpDate,
        message = message,
        isNotFavourite = isNotFavourite,
        articles = productList.map { it.uiOrderedProductData() },
    )
}

fun Result.OrderedProduct.dataToUiOrderedProduct(): UiState.OrderedProduct {
    return UiState.OrderedProduct(
        _id = id,
        _mode = mode,
        productId = productId,
        productName = productName,
        unit = unit,
        price = price,
        amount = amount,
        amountCount= amountCount,
        piecesCount = piecesCount
    )
}
fun UiState.OrderedProduct.uiOrderedProductData(): Result.OrderedProduct {
    return Result.OrderedProduct(
        productId = productId,
        productName = productName,
        unit = unit,
        price = price,
        amount = amount,
        amountCount = amountCount,
        piecesCount = piecesCount
    )
}

fun Result.BuyerProfile.dataToUiOrder(): UiState.BuyerProfile {
    return UiState.BuyerProfile(
        _mode = mode,
        displayName = displayName,
        emailAddress = emailAddress,
        isAnonymous = isAnonymous,
        photoUrl = photoUrl,
        phoneNumber = telephoneNumber,
        defaultMarket = defaultMarket,
        defaultTime = defaultPickUpTime
    )
}

fun UiState.BuyerProfile.uiBuyerProfileToData(): Result.BuyerProfile {
    return Result.BuyerProfile(
        displayName = displayName,
        emailAddress = emailAddress,
        isAnonymous = isAnonymous,
        photoUrl = photoUrl,
        telephoneNumber = phoneNumber ,
        defaultMarket = defaultMarket,
        defaultPickUpTime = defaultTime
    )
}


fun createBasketUDate(products: List<UiState.Article>, order: UiState.Order) : MutableList<UiState.Article> {
    val result: ArrayList<UiState.Article> = arrayListOf()
    order.productList.forEach { orderedProduct ->
        val available = products.firstOrNull { it._id == orderedProduct._id }
        if (available!=null){
            val copy = available.copy(
                amount = orderedProduct.amount,
                amountCount = orderedProduct.amountCount,
                priceDigit = orderedProduct.price)
            copy.pieceCounter = orderedProduct.piecesCount
            available.pieceCounter = orderedProduct.piecesCount
            available.amountCount = orderedProduct.amountCount
            result.add(copy)
        }
    }
    return result
}

inline fun <reified T : UiState> UiState.errorActions(profile: T, action: () -> Unit): Boolean {
    val toBeChecked =
        T::class.memberProperties.filter { !it.name.startsWith("_") }
    toBeChecked.forEach { prop ->
        val p = prop.get(profile) as String
        if (p.isEmpty()) {
            action()
            return false
        }
    }
    return true
}