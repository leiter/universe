package com.together.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.together.R
import com.together.base.BaseFragment
import com.together.databinding.FragmentClientProfileBinding
import com.together.utils.hideIme
import viewLifecycleLazy


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ClientProfileFragment : BaseFragment(R.layout.fragment_client_profile) {


    private val binding by viewLifecycleLazy { FragmentClientProfileBinding.bind(requireView()) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        setupTextFields()
    }

    private fun setupTextFields() {
        binding.tvPhoneNumber.setText(viewModel.buyerProfile.phoneNumber)
        binding.tvDisplayName.setText(viewModel.buyerProfile.displayName)
        binding.tvEmailAddress.setText(viewModel.buyerProfile.emailAddress)
    }


    private fun setupClicks(){
        binding.btnBack.setOnClickListener { activity?.onBackPressed() }
        binding.btnClearDisplayName.setOnClickListener { binding.tvDisplayName.setText("") }
        binding.btnClearEmail.setOnClickListener { binding.tvEmailAddress.setText("") }
        binding.btnClearPhoneNumber.setOnClickListener { binding.tvPhoneNumber.setText("") }
    }

    override fun onPause() {
        binding.root.clearFocus()
        binding.root.hideIme()
        super.onPause()

    }


}