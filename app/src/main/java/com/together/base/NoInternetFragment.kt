package com.together.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.together.R
import com.together.app.MainActivity
import com.together.databinding.FragmentNoInternetBinding
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
import com.together.utils.hasInternet
import viewBinding


class NoInternetFragment : BaseFragment(R.layout.fragment_no_internet) {

    private val viewBinding: FragmentNoInternetBinding by viewBinding {FragmentNoInternetBinding.bind(requireView())}

    private lateinit var vModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setBackgroundDrawable(ColorDrawable(0))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        requireActivity().window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        super.onDestroyView()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        viewBinding.btnRetry.setOnClickListener {
            if(requireActivity().hasInternet())FireBaseAuth.loginAnonymously()
            else Toast.makeText(requireContext(),"Noch keine Internetverbindung", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val TAG = "NoInternetFragment"
    }
}