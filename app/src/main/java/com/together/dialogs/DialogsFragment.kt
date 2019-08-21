package com.together.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.together.R


class DialogsFragment : DialogFragment() {

    private var layoutID: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            layoutID = it.getInt(ARG_DIALOG_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.add_product_dialog, container, false)

    companion object {

        private const val ARG_DIALOG_TYPE = "dialogType"

        @JvmStatic
        fun newInstance(layoutId: Int) =
            DialogsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_DIALOG_TYPE, layoutId)
                }
            }
    }
}
