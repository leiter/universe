package com.together.chat.anyidea

import com.google.firebase.database.DatabaseReference
import com.together.repository.Result
import com.together.repository.auth.FirebaseAuth
import com.together.repository.storage.FireData


class AnyIdeaPresenter(val dataRef: DatabaseReference) {

    fun postAnyMessage(msg: String) {
        if (FirebaseAuth.fireUser != null) {
            val user = FirebaseAuth.fireUser
            val message = Result.ChatMessage(
                "",
                user.uid,
                user.displayName ?: " ",
                msg, user.photoUrl?.toString() ?: ""
            )
            FireData.createDocument(dataRef, "anymessage", message)
        }
    }


    private fun fire(): FireData = FireData()
}