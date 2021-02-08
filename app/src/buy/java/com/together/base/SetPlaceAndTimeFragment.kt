package com.together.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.together.databinding.FragmentSetPlaceAndTimeBinding
import viewBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SetPlaceAndTimeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SetPlaceAndTimeFragment : DialogFragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: MainViewModel by viewModels({ requireParentFragment() })
    private val viewBinding: FragmentSetPlaceAndTimeBinding by viewBinding (FragmentSetPlaceAndTimeBinding::inflate)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return viewBinding.root
    }

    companion object {

        const val TAG = "SetPlaceAndTimeFragment"
        @JvmStatic fun newInstance(param1: String, param2: String) =
                SetPlaceAndTimeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}