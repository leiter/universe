package com.together.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.together.R
import com.together.databinding.FragmentNoInternetBinding
import com.together.repository.auth.FireBaseAuth
import com.together.utils.isInternetAvailable
import viewBinding


class NoInternetFragment : BaseFragment(R.layout.fragment_no_internet) {

    private val viewBinding: FragmentNoInternetBinding by viewBinding {FragmentNoInternetBinding.bind(requireView())}

    private lateinit var vModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setBackgroundDrawable(ResourcesCompat.getDrawable(
            resources,R.drawable.splash,requireActivity().theme))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        requireActivity().window.setBackgroundDrawable(ResourcesCompat.getDrawable(
            resources,R.drawable.splash,requireActivity().theme))
        super.onResume()
    }

    override fun onDestroyView() {
        requireActivity().window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        super.onDestroyView()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        viewBinding.btnRetry.setOnClickListener {
            isInternetAvailable().subscribe({
                if (it) FireBaseAuth.loginAnonymously()
                else Toast.makeText(
                    requireContext(),
                    "Noch keine Internetverbindung",
                    Toast.LENGTH_LONG
                ).show()
            }, {


            })
        }
    }
    companion object {
        const val TAG = "NoInternetFragment"
    }
}