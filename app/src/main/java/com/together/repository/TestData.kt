package com.together.repository

import com.together.app.UiState


object TestData {

    val articleList = listOf(
        Result.Article(
            0,
            "Banana",
            "Nice fruit to eat.",
            true
        ),
        Result.Article(
            1,
            "Apple",
            "Nice fruit to eat.",
            false
        ),
        Result.Article(
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
            true
        ),
        UiState.Article(
            1,
            "Apple",
            "Nice fruit to eat.",
            false
        ),
        UiState.Article(
            2,
            "Potato",
            "Lovely vegetable.",
            true
        ),
        UiState.Article(
            0,
            "Banana",
            "Nice fruit to eat.",
            true
        ),
        UiState.Article(
            1,
            "Apple",
            "Nice fruit to eat.",
            false
        ),
        UiState.Article(
            2,
            "Potato",
            "Lovely vegetable.",
            true
        )

    )

}