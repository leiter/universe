package com.together

import com.together.base.UiState
import com.together.repository.Result
import java.util.*

object TestData {

    val articleList = listOf(
        Result.Article(
            "",
            -1,
            "0",
            "Feigenbananen",
            true,
            imageUrl =  "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2F1609505109087_tmp6255790586712894073.tmp?alt=media&token=df7af22c-7c5a-4764-837c-073376372b9d",
            price = 2.3,
            unit = "kg",
            category = "Bananen",
            detailInfo = "Demeter Biobananen. Die Bananen stammen aus Peru und sind nach Vorgaben der Demeter angebaut.",
            searchTerms = "Banane,",
            weighPerPiece = 0.130

        ),
        Result.Article(
            "",
            -1,
            "1",
            "Granny Smith",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2F1609270827791_tmp1052065387795357072.tmp?alt=media&token=3ae17db0-a140-4a3d-bd6d-2843cfd5b36e",
            price = 2.3,
            unit = "kg",
            category = "Apfel",
            detailInfo = "Leicht säuerlicher Apfel. Die Äpfel wurden in Stralsund geerntet und sind mit dem europäischen Biosiegel versehen. Die Äpfel wurden nicht gespritzt und auch nicht gewachst.",
            searchTerms = "Apfel,Äpfel",
            weighPerPiece = 0.092

        ),
        Result.Article(
            "",
            -1,
            "2",
            "Linda Kartoffeln",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2Ftmp1576373532957500855.tmp?alt=media&token=81892fcd-c346-479a-8b56-4b56d7ce8381",
            price = 2.3,
            unit = "kg",
            category = "Kartoffel",
            detailInfo = "Festkochende Kartoffel vom Hof Apfeltraum. Die Kartoffeln wurden in dem Biohof Wizenau bei Buxdehude nach den Anforderungen des europäischen Biosiegels angebaut.",
            searchTerms = "Kartoffel,Kartoffeln",
            weighPerPiece = 0.060
        ),
        Result.Article(
            "",
            -1,
            "3",
            "Stangensellerie",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2Ftmp5874223768482947196.tmp?alt=media&token=a3ca5626-0ff4-473f-a289-a8cf090c82f5",
            price = 2.9,
            unit = "Stück",
            category = "Stangensellerie",
            detailInfo = "Aus der Umgebung. Die Kartoffeln wurden in dem Biohof Wizenau bei Buxdehude nach den Anforderungen des europäischen Biosiegels angebaut.",
            weighPerPiece = 1.0
        ),
        Result.Article(
            "",
            -1,
            "4",
            "Atomic Red",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2F1609360024895_tmp7985643096611096660.tmp?alt=media&token=e0fcf888-301f-4172-ac46-7a842710b4e8",
            price = 3.69,
            unit = "Bund",
            category = "Karotten",
            detailInfo = "Vom Biohof Waldheide. Die Karotten wurden in dem Biohof Wizenau bei Buxdehude nach den Anforderungen des europäischen Biosiegels angebaut.",
            weighPerPiece = 1.0,
            searchTerms = "Möhren,Möhre,Karotte,Karotten,"
        ),
        Result.Article(
            "",
            -1,
            "5",
            "Erdbeeren",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2Ftmp1145260240680560593.tmp?alt=media&token=0670c0da-e260-4d41-b5d8-1a119ea24a64",
            price = 5.69,
            unit = "Schale",
            category = "Erdbeeren",
            detailInfo = "Vom Biohof Waldheide. Die Erdbeeren stammen aus Friedenau vom Hof Bio Müller. Bei der Aufzucht wurden keinerlei Insektiziede verwendet.",
            weighPerPiece = 1.0,
            searchTerms = "Erdbeere"
        ),
        Result.Article(
            "",
            -1,
            "6",
            "Knoblauch",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2F1609405381918_tmp466197465066176923.tmp?alt=media&token=24a0c4fc-342e-4c64-a807-fadbaea82e98",
            price = 10.69,
            unit = "kg",
            category = "Knoblauch",
            detailInfo = "Vom Biohof Waldheide. Der Knoblauch wurde nach den Produktionsvorgaben von Demeter erzeugt.",
            weighPerPiece = 0.030
        ),
        Result.Article(
            "",
            -1,
            "2",
            "Siglinde Kartoffeln",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2Ftmp1576373532957500855.tmp?alt=media&token=81892fcd-c346-479a-8b56-4b56d7ce8381",
            price = 2.9,
            unit = "kg",
            category = "Kartoffeln",
            detailInfo = "Festkochende Kartoffel vom Hof Apfeltraum. Die Kartoffeln wurden in dem Biohof Wizenau bei Buxdehude nach den Anforderungen des europäischen Biosiegels angebaut.",
            weighPerPiece = 0.090,
            searchTerms = "Kartoffel,"
        ),
        Result.Article(
            "",
            -1,
            "1",
            "Boskop",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2F1609270827791_tmp1052065387795357072.tmp?alt=media&token=3ae17db0-a140-4a3d-bd6d-2843cfd5b36e",
            price = 2.3,
            unit = "kg",
            category = "Apfel",
            detailInfo = "Saftig und sauer. Die Äpfel wurden in Stralsund geerntet und sind mit dem europäischen Biosiegel versehen. Die Äpfel wurden nicht gespritzt und auch nicht gewachst.",
            weighPerPiece = 0.115,
            searchTerms = "Äpfel,"

        ),
        Result.Article(
            "",
            -1,
            "1",
            "Elstar",
            true,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/fire-one-58ddc.appspot.com/o/images%2F1609270827791_tmp1052065387795357072.tmp?alt=media&token=3ae17db0-a140-4a3d-bd6d-2843cfd5b36e",
            price = 2.3,
            unit = "kg",
            category = "Apfel",
            detailInfo = "Süß-sauerer Apfel. Die Äpfel wurden in Stralsund geerntet und sind mit dem europäischen Biosiegel versehen. Die Äpfel wurden nicht gespritzt und auch nicht gewachst.",
            weighPerPiece = 0.098,
            searchTerms = "Äpfel"
        ),
    )


    private val userList = listOf(
        Result.BuyerProfile(
            displayName = "Marco",
            emailAddress = "mmaleiter@gmail.com"
        ),
        Result.BuyerProfile(
            displayName = "Mandy Leiter",
            emailAddress = "marcoleiter@arcor.de"
        ),
    )

   private val marketListItems = listOf(
        Result.Market(
            name = "Wochenmarkt Onkel Toms Hütte",
            street = "Onkel-Tom-Straße",
            houseNumber = "99",
            zipCode = "14169",
            city = "Berlin",
            begin = "12:00",
            end = "18:30",
            dayOfWeek = "Donnerstag",
            dayIndex = Calendar.THURSDAY

        ),
        Result.Market(
            name = "Ökomarkt im Hansaviertel",
            street = "Altonaer Str.",
            houseNumber = "18",
            zipCode = "10557",
            city = "Berlin",
            begin = "12:00",
            end = "18:30",
            dayOfWeek = "Freitag",
            dayIndex = Calendar.FRIDAY
        )
    )

    val sellerProfile = Result.SellerProfile(
        displayName = "BodenSchätze",
        firstName = "Eric",
        lastName = "Dehn",
        street = "Frankfurther Allee",
        houseNumber = "27",
        city = "Berlin",
        zipCode = "14195",
        telephoneNumber = "01724623741",
        markets = mutableListOf(marketListItems[0],marketListItems[1]),
    )




    val orderList = listOf(
        Result.Order(
            id = "",
            buyerProfile = userList[0],
//            articles = articleList
        ),
        Result.Order(
            id = "",
            buyerProfile = userList[1],
//            articles = articleList
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