package com.together.repository

import com.together.app.UiState


object TestData {

    val articleList = listOf(
        Result.Article(
            "",
            0,
            "Banana",
            "Nice fruit to eat.",
            true
        ),
        Result.Article(
            "",
            1,
            "Apple",
            "Nice fruit to eat.",
            false
        ),
        Result.Article(
            "",
            2,
            "Potato",
            "Lovely vegetable.",
            true
        )


    )

    val userList = listOf(
        Result.User(displayName = "Marco", emailAdress = "mmaleiter@gmail.com"),
        Result.User(displayName = "Mandy", emailAdress = "marcoleiter@arcor.de"),
        Result.User(displayName = "Annabel", emailAdress = "")
    )





    val uiArticleList = mutableListOf(
        UiState.Article(
            0,
            "Banana",
            "Nice fruit to eat.",
            true,
            "kg",
            "2,30",
            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
        ),
        UiState.Article(
            1,
            "Apple",
            "Nice fruit to eat.",
            false,
            "kg",
            "2,30",
            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
        ),
        UiState.Article(
            2,
            "Potato",
            "Lovely vegetable.",
            true,
            "kg",
            "2,30",
            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
        ),
        UiState.Article(
            0,
            "Banana",
            "Nice fruit to eat.",
            true,
            "kg",
            "2,30",
            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
        ),
        UiState.Article(
            1,
            "Apple",
            "Nice fruit to eat.",
            false,
            "kg",
            "2,30",
            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
        ),
        UiState.Article(
            2,
            "Potato",
            "Lovely vegetable.",
            true,
            "kg",
            "2,30",
            "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/4_obst.png?alt=media&token=186ac0b8-c7df-4294-9ad0-0675ed4a49de"
        )

    )

}