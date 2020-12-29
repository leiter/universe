package com.together

import com.together.base.UiState
import com.together.repository.Result

object TestData {

    val articleList = listOf(
        Result.Article(
            "",
            -1,
            0,
            "Banana",
            "Nice fruit to eat.",
            true,
            imageUrl =  "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2Ftmp1309847000452879291.tmp?alt=media&token=7bfd74a2-30fa-48f8-b4ec-e0b6fc29fc57"
        ),
        Result.Article(
            "",
            -1,
            1,
            "Apple",
            "Nice fruit to eat.",
            false,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2Ftmp3425451583087693428.tmp?alt=media&token=1533803b-a0f4-486e-95ad-39a7abf06df1"

        ),
        Result.Article(
            "",
            -1,
            2,
            "Potato",
            "Lovely vegetable.",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2F35e1cd8a-458b-4308-b4d1-2d0bbb8c3369.jpg?alt=media&token=f2a50556-fe36-4bfc-81e4-27ae2324bbd2"
        )
    )



    val userList = listOf(
        Result.User(
            displayName = "Marco",
            emailAddress = "mmaleiter@gmail.com"
        ),
        Result.User(
            displayName = "Mandy Leiter",
            emailAddress = "marcoleiter@arcor.de"
        ),
        Result.User(
            displayName = "Annabel",
            emailAddress = ""
        )
    )

    val sellerProfile = Result.SellerProfile(
        displayName = "BodenSchätze",
        firstName = "Eric",
        lastName = "Dehn",
        street = "Frankfurther Allee",
        houseNumber = "27",
        city = "Berlin",
        zipcode = "14195"
    )

    val orderList = listOf(
        Result.Order(
            id = "",
            user = userList[0],
            articles = articleList
        ),
        Result.Order(
            id = "",
            user = userList[1],
            articles = articleList
        )

    )

    val uiArticleList = mutableListOf<UiState.Article>(
//        UiState.Article(
//            "0",
//            "Banana",
//            "Nice fruit to eat.",
//            true,
//            "kg",
//            "2,30",
//            "https://firebasestorage.googleapis.com/v0/b/" +
//                    "fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
//        ),
//        UiState.Article(
//            1,
//            "Apple",
//            "Nice fruit to eat.",
//            false,
//            "kg",
//            "2,30",
//            "https://firebasestorage.googleapis.com/v0/b/" +
//                    "fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
//        ),
//        UiState.Article(
//            2,
//            "Potato",
//            "Lovely vegetable.",
//            true,
//            "kg",
//            "2,30",
//            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
//        ),
//        UiState.Article(
//            0,
//            "Banana",
//            "Nice fruit to eat.",
//            true,
//            "kg",
//            "2,30",
//            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
//        ),
//        UiState.Article(
//            1,
//            "Apple",
//            "Nice fruit to eat.",
//            false,
//            "kg",
//            "2,30",
//            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
//        ),
//        UiState.Article(
//            2,
//            "Potato",
//            "Lovely vegetable.",
//            true,
//            "kg",
//            "2,30",
//            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
//        )

    )

}