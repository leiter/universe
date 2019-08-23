package com.together.chat.anyidea

import com.google.firebase.database.DatabaseReference
import com.together.repository.Result
import com.together.repository.Result.Companion.MOVED
import com.together.repository.auth.FireBaseAuth
import com.together.repository.storage.FireData


class AnyIdeaPresenter(val dataRef: DatabaseReference) {

    fun postAnyMessage(msg: String) {
        if (FireBaseAuth.getAuth()!!.currentUser != null) {
            val user = FireBaseAuth.getAuth()!!.currentUser!!
            val message = Result.ChatMessage(
                "",
                MOVED,
                user.uid,
                user.displayName ?: " ",
                msg, user.photoUrl?.toString() ?: ""
            )
            FireData.createDocument(dataRef, "anymessage", message)
        }
    }


}
