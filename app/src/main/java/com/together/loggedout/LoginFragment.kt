package com.together.loggedout

import android.os.Bundle
import android.view.View
import com.together.R
import com.together.app.MainActivity
import com.together.base.BaseFragment
import com.together.base.UiState
import com.together.databinding.FragmentLoginBinding
import com.together.utils.viewLifecycleLazy

class LoginFragment : BaseFragment(R.layout.fragment_login) {
    companion object {
        const val TAG = "LoginFragment"
    }

    private val viewBinding: FragmentLoginBinding by viewLifecycleLazy { FragmentLoginBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.plusOneButton.setOnClickListener {
//            UtilsActivity.startGoogleSigning(requireContext())
            MainActivity.startLogin(requireActivity())
            it.visibility = View.GONE
            viewModel.loggedState.observe(viewLifecycleOwner, { uiState ->
                if(uiState is UiState.LoggedOut){
                } else {
                    viewBinding.plusOneButton.visibility = View.VISIBLE
                    viewBinding.plusOneButton.visibility = View.GONE
                }
            })
        }
    }
}
