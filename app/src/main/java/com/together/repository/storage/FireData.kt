package com.together.repository.storage

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.UploadTask
import com.together.repository.Result
import io.reactivex.Single

object FireData {

    private val fireRef = FirebaseDatabase.getInstance().reference

    fun createDocument(ref: DatabaseReference, path: String, document: Result) {
        ref.child(path).push().setValue(document)
    }

    fun createDocument(path: String, document: Result)  {
        FirebaseDatabase.getInstance().reference.child(path).push().setValue(document)
    }


    fun doesNodeExist(childList:List<String>) : Single<Boolean>{
        val target = fireRef.root
        childList.forEach {
            target.child(it)
        }
        return fireRef.getSingle(childList[0])
    }




}


fun  UploadTask.getSingle(): Single<UploadTask.TaskSnapshot> {
    return Single.create {
            emitter ->

        val listener = object : OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                emitter.onSuccess(p0!!

//                    Result.NewImageCreated(p0?.toString()?:"n/a",p0?.uploadSessionUri!!)

                )
                removeOnSuccessListener(this)
            }

        }

        addOnSuccessListener(listener)
        emitter.setCancellable { removeOnSuccessListener(listener) }
    }
}