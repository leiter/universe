package com.together.addpicture

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.together.databinding.ChooseSourceBinding
import com.together.utils.viewBinding
import io.reactivex.subjects.PublishSubject


class ChooseDialog : DialogFragment() {

    val actionChannel: PublishSubject<Action> = PublishSubject.create()

    private val viewBinding: ChooseSourceBinding by viewBinding(ChooseSourceBinding::inflate )

    sealed class Action {
        object CHOOSE_PICTURE : Action()
        object TAKE_PICKTURE : Action()
        object DELETE_PHOTO : Action()  //path param
        object CANCEL_ADD_PICTURE : Action()
    }

    companion object {
        const val TAG = "AddPicture"  // Handle hideIme delete
    }

    override fun onCancel(dialog: DialogInterface) {
        actionChannel.onNext(Action.CANCEL_ADD_PICTURE)
        super.onCancel(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(viewBinding.root)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding.btnCaptureImage.setOnClickListener { actionChannel.onNext(Action.TAKE_PICKTURE); dismiss() }
        viewBinding.btnImportImage.setOnClickListener { actionChannel.onNext(Action.CHOOSE_PICTURE); dismiss() }
        viewBinding.btnCancel.setOnClickListener { actionChannel.onNext(Action.CANCEL_ADD_PICTURE); dismiss() }
        viewBinding.btnDeleteImage.setOnClickListener { actionChannel.onNext(Action.DELETE_PHOTO); dismiss() }
        return viewBinding.root
    }

}
