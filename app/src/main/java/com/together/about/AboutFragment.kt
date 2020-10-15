package com.together.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        show_licences.setOnClickListener {
            MainMessagePipe.uiEvent.onNext(UiEvent.ShowLicense(requireContext()))
        }
        back_button.setOnClickListener { activity?.onBackPressed() }
    }

    companion object {
        const val TAG = "AboutFragment"
    }
}
