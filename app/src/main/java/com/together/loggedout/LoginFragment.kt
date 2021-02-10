package com.together.loggedout

import android.os.Bundle
import android.view.View
import com.together.R
import com.together.app.MainActivity
import com.together.base.BaseFragment
import com.together.base.UiState
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : BaseFragment(R.layout.fragment_login) {
    companion object {
        const val TAG = "LoginFragment"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plus_one_button.setOnClickListener {
//            UtilsActivity.startGoogleSigning(requireContext())
            MainActivity.startLogin(requireActivity())
            it.visibility = View.GONE
            viewModel.loggedState.observe(viewLifecycleOwner, { uiState ->
                if(uiState is UiState.LoggedOut){
                    plus_one_button.visibility = View.VISIBLE
                } else {
                    plus_one_button.visibility = View.GONE
                }
            })
        }
    }
}
