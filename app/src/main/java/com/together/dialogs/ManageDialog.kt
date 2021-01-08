package com.together.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import kotlinx.android.synthetic.main.choose_source.view.*
import kotlinx.android.synthetic.main.choose_source.view.choose_image
import kotlinx.android.synthetic.main.manage_dialog.view.*

class ManageDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.manage_dialog, null as ViewGroup?)
        view.btn_profile.setOnClickListener { ; dismiss() }
        view.choose_image.setOnClickListener { ; dismiss() }
        view.btn_log_out.setOnClickListener { MainMessagePipe.uiEvent.onNext(UiEvent.LogOut); dismiss() }
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }
}