package com.together.profile

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.PopupMenu
import com.together.R
import com.together.base.BaseFragment
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.databinding.FragmentClientProfileBinding
import com.together.utils.hideIme
import kotlinx.android.synthetic.main.fragment_about.*
import viewLifecycleLazy


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ClientProfileFragment : BaseFragment(R.layout.fragment_client_profile) {


    private val viewBinding by viewLifecycleLazy { FragmentClientProfileBinding.bind(requireView()) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        setupTextFields()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun setupTextFields() {
        viewBinding.tvPhoneNumber.setText(viewModel.buyerProfile.phoneNumber)
        viewBinding.tvDisplayName.setText(viewModel.buyerProfile.displayName)
        viewBinding.tvEmailAddress.setText(viewModel.buyerProfile.emailAddress)
    }


    private fun setupClicks(){
        viewBinding.btnBack.setOnClickListener { activity?.onBackPressed() }
        viewBinding.btnClearDisplayName.setOnClickListener { viewBinding.tvDisplayName.setText("") }
        viewBinding.btnClearEmail.setOnClickListener { viewBinding.tvEmailAddress.setText("") }
        viewBinding.btnClearPhoneNumber.setOnClickListener { viewBinding.tvPhoneNumber.setText("") }
        viewBinding.btnOptions.setOnClickListener { showPopup() }
        viewBinding.btnSaveProfile.setOnClickListener {  }
    }

    override fun onPause() {
        viewBinding.root.clearFocus()
        viewBinding.root.hideIme()
        super.onPause()
    }

    override fun onDestroyView() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onDestroyView()
    }

    private fun showPopup() {
        val popupMenu = PopupMenu(requireActivity(), viewBinding.btnOptions)
        popupMenu.menuInflater.inflate(R.menu.menu_setting_client_profile,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.btn_clear_data -> {
//                    MainMessagePipe.uiEvent.onNext(UiEvent.ShowLicense(requireContext()))
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