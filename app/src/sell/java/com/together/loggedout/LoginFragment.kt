package com.together.loggedout

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.together.R
import com.together.app.MainActivity
import com.together.base.BaseFragment
import com.together.base.UiState
import com.together.databinding.FragmentLoginBinding
import com.together.repository.Database
import com.together.repository.storage.getSingleExists
import com.together.utils.ACTION_SHOW_ORDER_FRAGMENT
import com.together.utils.viewLifecycleLazy


class LoginFragment : BaseFragment(R.layout.fragment_login) {
    companion object {
        const val TAG = "LoginFragment"
    }

    private val viewBinding: FragmentLoginBinding by viewLifecycleLazy { FragmentLoginBinding.bind(requireView()) }

    private lateinit var navCon: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nf = requireActivity().supportFragmentManager
            .findFragmentById(R.id.navigation_controller) as NavHostFragment
        navCon = nf.navController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loggedState.observe(viewLifecycleOwner, {
            if (it is UiState.BaseAuth) {
                Database.sellerProfile("", true).getSingleExists().subscribe { exists ->
                    if (exists) {
                        if(requireActivity().intent.action == ACTION_SHOW_ORDER_FRAGMENT) {
                            navCon.navigate(R.id.showOrdersFragment)
                        } else {
                            navCon.navigate(R.id.createFragment)
                        }
                    } else {
                        findNavController().navigate(R.id.profileFragment)

                    }
                }
            }
        })

        viewBinding.plusOneButton.setOnClickListener {
//            UtilsActivity.startGoogleSigning(requireContext())
            MainActivity.startLogin(requireActivity())
            it.visibility = View.GONE
            viewModel.loggedState.observe(viewLifecycleOwner, { uiState ->
                if(uiState !is UiState.LoggedOut){
                    viewBinding.plusOneButton.visibility = View.VISIBLE
                    viewBinding.plusOneButton.visibility = View.GONE
                }
            })
        }
    }
}
