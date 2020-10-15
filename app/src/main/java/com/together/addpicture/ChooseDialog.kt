package com.together.addpicture

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
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
        class DELETE_PHOTO : Action()  //path param
        object CANCEL_ADD_PICTURE : Action()
    }

    companion object {
        const val TAG = "AddPicture"  // Handle hideIme delete
        fun newInstance(): ChooseDialog {
            val frag = ChooseDialog()
//            val args: Bundle = Bundle().apply { put }
            return frag
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        actionChannel.onNext(Action.CANCEL_ADD_PICTURE)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.choose_source, null as ViewGroup?)
        view.capture_image.setOnClickListener { actionChannel.onNext(Action.TAKE_PICKTURE); dismiss() }
        view.choose_image.setOnClickListener { actionChannel.onNext(Action.CHOOSE_PICTURE); dismiss() }
        view.cancel.setOnClickListener { actionChannel.onNext(Action.CANCEL_ADD_PICTURE); dismiss() }
        view.delete_image.setOnClickListener { actionChannel.onNext(Action.DELETE_PHOTO()); dismiss() }
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        return dialog

    }

}
