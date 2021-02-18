package com.together.dialogs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.together.databinding.FragmentInfoDialogBinding
import com.together.utils.viewBinding


class InfoDialogFragment : DialogFragment() {

    private var usageMode: String? = null
    private var productInfo: String? = null

    private val viewBinding : FragmentInfoDialogBinding by viewBinding( FragmentInfoDialogBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE,0)
        requireArguments().let {
            usageMode = it.getString(USAGE_MODE)
            productInfo = it.getString(PRODUCT_INFO)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
//        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewBinding.tvProductInfo.text = productInfo
        return viewBinding.root
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
