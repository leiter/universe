package com.together.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.together.R
import com.together.databinding.FragmentClientProfileBinding
import viewLifecycleLazy


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ClientProfileFragment : Fragment(R.layout.fragment_client_profile) {


    private val binding by viewLifecycleLazy { FragmentClientProfileBinding.bind(requireView()) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { activity?.onBackPressed() }
    }


}