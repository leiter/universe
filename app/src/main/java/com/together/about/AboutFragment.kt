package com.together.about

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import com.together.R
import com.together.base.BaseFragment
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.databinding.FragmentAboutBinding
import com.together.utils.getIntIdentity
import com.together.utils.viewBinding

class AboutFragment : BaseFragment(R.layout.fragment_about) {

    private val viewBinding: FragmentAboutBinding by viewBinding { FragmentAboutBinding.bind(requireView()) }

    private lateinit var flavour: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flavour = it.getString("navigationId_back_btn") ?: ""
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.btnSettings.setOnClickListener { showPopup() }
        viewBinding.backButton.setOnClickListener {
            if(flavour=="createFragment") {  // later use id reference directly
                findNavController().navigate(requireContext().getIntIdentity(flavour,"id"))
            } else {
                requireActivity().onBackPressed()
            }
        }
    }

    companion object {
        const val TAG = "AboutFragment"
    }

    private fun showPopup() {
        val popupMenu = PopupMenu(requireActivity(), viewBinding.btnSettings)
        popupMenu.menuInflater.inflate(R.menu.menu_setting_about, popupMenu.menu)
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
