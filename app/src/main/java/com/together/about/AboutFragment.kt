package com.together.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.PopupMenuCompat
import androidx.fragment.app.Fragment
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_settings.setOnClickListener { showPopup()}
        back_button.setOnClickListener { activity?.onBackPressed() }
    }

    companion object {
        const val TAG = "AboutFragment"
    }

    private fun showPopup() {
        val popupMenu = PopupMenu(requireActivity(), btn_settings)
        popupMenu.menuInflater.inflate(R.menu.menu_setting_about,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.btn_show_licence -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.ShowLicense(requireContext()))
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }
        popupMenu.show()
    }

}
