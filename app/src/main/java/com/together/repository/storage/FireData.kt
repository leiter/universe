package com.together.repository.storage

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.UploadTask
import com.together.repository.Result
import io.reactivex.Single

class FireData {

    fun createDocument(ref: DatabaseReference, path: String, document: Result) {
        ref.child(path).push().setValue(document)
    }

    fun createDocument(path: String, document: Result)  {
        FirebaseDatabase.getInstance().reference.child(path).push().setValue(document)
    }


}


fun  UploadTask.getSingle(): Single<out Result> {
    return Single.create { emitter ->

        val listener = object : OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                emitter.onSuccess(Result.NewImageCreated(p0?.toString()?:"n/a",p0?.uploadSessionUri!!))
                removeOnSuccessListener(this)
            }

        }
        addOnSuccessListener(listener)
        emitter.setCancellable { removeOnSuccessListener(listener) }
    }
}