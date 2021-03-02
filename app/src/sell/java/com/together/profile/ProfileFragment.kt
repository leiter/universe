package com.together.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.app.MarketDialog
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.base.UtilsActivity
import com.together.databinding.FragmentProfileBinding
import com.together.utils.hide
import com.together.utils.show
import com.together.utils.viewLifecycleLazy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class ProfileFragment() : Fragment(R.layout.fragment_profile) {

    val viewModel: ProfileViewModel by viewModels()

    private val disposable = CompositeDisposable()

    companion object {
        const val TAG = "ProfileFragment"
    }

    private val viewBinding: FragmentProfileBinding by viewLifecycleLazy {
        FragmentProfileBinding.bind(requireView())
    }

    private var hasBackButton: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hasBackButton = it.getBoolean("with_back_btn")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            city.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.city = it.toString()
            }.addTo(disposable)
            zipCode.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.zipCode = it.toString()
            }.addTo(disposable)
            house.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.houseNumber = it.toString()
            }.addTo(disposable)
            street.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.street = it.toString()
            }.addTo(disposable)
            companyName.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.displayName = it.toString()
            }.addTo(disposable)
            firstName.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.firstName = it.toString()
            }.addTo(disposable)
            lastName.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.lastName = it.toString()
            }.addTo(disposable)
            addPickupPlace.setOnClickListener {
                MarketDialog().show(childFragmentManager, MarketDialog.MARKET_DIALOG_TAG)
            }
            postProfile.setOnClickListener {
                viewModel.uploadSellerProfile()
//            if (!errorHints(i)) return@setOnClickListener
            }
            btnShowMangeImage.setOnClickListener {
                UtilsActivity.startAddImage(requireActivity())
            }

            if(hasBackButton!!) {
                btnGoBack.show()
                btnGoBack.setOnClickListener {
                    findNavController().navigate(R.id.action_profileFragment_to_createFragment) }
            } else btnGoBack.hide()
        }


        viewModel.profileLive.observe(viewLifecycleOwner,{ profile ->

            with(viewBinding) {
                lastName.setText(profile.lastName)
                firstName.setText(profile.firstName)
                companyName.setText(profile.displayName)
                house.setText(profile.houseNumber)
                zipCode.setText(profile.zipCode)
                city.setText(profile.city)
                street.setText(profile.street)
            }
        })

        viewModel.profileLive.observe(viewLifecycleOwner, {
            viewBinding.placesList.removeAllViews()
            val adapter = MarketAdapter(requireContext(), it.marketList, openAddMarket)
            (0 until adapter.count).forEach { pos ->
                val item = adapter.getView(pos, null, viewBinding.placesList)
                viewBinding.placesList.addView(item)
            }
        })

        viewModel.blockingLoaderState.observe(viewLifecycleOwner) {
            when (it) {
                is UiEvent.LoadingDone -> {
                    viewBinding.loadingIndicator.visibility = View.GONE
                }
                is UiEvent.Loading -> viewBinding.loadingIndicator.visibility = View.VISIBLE
                is UiEvent.ShowCreateFragment -> {
                    viewBinding.loadingIndicator.visibility = View.GONE
                    viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral(-1)
                    findNavController().navigate(R.id.createFragment)
                }
            }
        }
    }

    private val openAddMarket: (UiState.Market) -> Unit
        inline get() = {
            MarketDialog.newInstance(MarketDialog.EDIT_MARKET, it).show(
                childFragmentManager, MarketDialog.MARKET_DIALOG_TAG
            )
        }

    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }
}