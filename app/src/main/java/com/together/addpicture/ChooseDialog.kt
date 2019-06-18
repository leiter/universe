package com.together.addpicture

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.together.R
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.choose_source.view.*


class ChooseDialog : DialogFragment() {

    val actionChannel: PublishSubject<Action> = PublishSubject.create()

    sealed class Action {
        object CHOOSE_PICTURE : Action()
        object TAKE_PICKTURE : Action()
        class DELETE_PHOTO() : Action()  //path param
        object CANCEL_ADD_PICTURE : Action()
        class ActivityResult(
            val requestCode: Int,
            val resultCode: Int,
            val data: Intent?
        ) : Action()

        class PermissionResult(
            val requestCode: Int,
            val permissions: Array<out String>,
            val grantResults: IntArray
        ) : Action()


    }


    companion object {
        const val TAG = "AddPicture"  // Handle hide delete
        fun newInstance(): ChooseDialog {
            val frag = ChooseDialog()
//            val args: Bundle = Bundle().apply { put }
            return frag
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        actionChannel.onNext(Action.ActivityResult(requestCode, resultCode, data))
        Log.e("TTTTT", "For debugging onActivity result");
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        actionChannel.onNext(Action.PermissionResult(requestCode, permissions, grantResults))
        Log.e("TTTTT", "For debugging onRequest Permission");
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val view = activity!!.layoutInflater.inflate(R.layout.choose_source, null as ViewGroup?)
        view.capture_image.setOnClickListener { actionChannel.onNext(Action.TAKE_PICKTURE); dismiss() }
        view.choose_image.setOnClickListener { actionChannel.onNext(Action.CHOOSE_PICTURE); dismiss() }
        view.cancel.setOnClickListener { actionChannel.onNext(Action.CANCEL_ADD_PICTURE); dismiss() }
        view.delete_image.setOnClickListener { actionChannel.onNext(Action.DELETE_PHOTO()); dismiss() }
        builder.setView(view)
        return builder.create()

    }

}
