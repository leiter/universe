package com.together.repository.storage

import com.google.firebase.database.DatabaseReference
import com.together.repository.Result

object FireData {


    fun createDocument(ref: DatabaseReference, path: String, document: Result) {
        ref.child(path).push().setValue(document)

    }


}
