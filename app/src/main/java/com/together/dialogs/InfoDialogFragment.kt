package com.together.dialogs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.together.R



class InfoDialogFragment : DialogFragment() {

    private var usageMode: String? = null
    private var param2: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            usageMode = it.getString(USAGE_MODE)
            param2 = it.getString(PRODUCT_INFO)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_info_dialog, container, false)

        v.findViewById<TextView>(R.id.tv_product_info).text = param2
        return v
    }


    companion object {

        const val TAG = "InfoDialogFragment"
        private const val USAGE_MODE = "usageMode"
        private const val PRODUCT_INFO = "product_info"

        const val SHOW_INFO = "show_info"
        const val EDIT_INFO = "edit_info"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InfoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(USAGE_MODE, param1)
                    putString(PRODUCT_INFO, param2)
                }
            }
    }
}
