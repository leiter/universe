package com.together.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.app.MarketDialog
import com.together.base.*
import com.together.create.CreateFragment
import com.together.databinding.FragmentProfileBinding
import com.together.utils.loadImage
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class ProfileFragment : BaseFragment(R.layout.fragment_profile) {

    companion object {
        const val TAG = "ProfileFragment"
    }

    private lateinit var viewBinding : FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentProfileBinding.inflate(inflater,container,false)

        with(viewBinding){
            city.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.city = it.toString()
            }.addTo(disposable)
            zipcode.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
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
        }

        viewBinding.postProfile.setOnClickListener {
            viewModel.uploadSellerProfile()
//            if (!errorHints(i)) return@setOnClickListener
        }
        viewBinding.btnShowMangeImage.setOnClickListener {
            UtilsActivity.startAddImage(requireActivity())
        }

        viewModel.newProduct.observe(viewLifecycleOwner, {
            requireContext().loadImage(viewBinding.storeImage,it.uri.toString())
        })

        viewModel.markets.observe(viewLifecycleOwner, {
            viewBinding.placesList.removeAllViews()
            val adapter = MarketAdapter(requireContext(), it, openAddMarket)
            (0 until adapter.count).forEach { pos ->
                val item = adapter.getView(pos, null, viewBinding.placesList)
                viewBinding.placesList.addView(item)
            }
        })

        viewModel.blockingLoaderState.observe(viewLifecycleOwner) {
            when (it ) {
                is UiEvent.LoadingDone ->  viewBinding.loadingIndicator.visibility = View.GONE
                is UiEvent.Loading -> viewBinding.loadingIndicator.visibility = View.VISIBLE
                is UiEvent.ShowCreateFragment -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.ReplaceFragment(
                        requireActivity().supportFragmentManager, CreateFragment(), CreateFragment.TAG))
                }
            }
        }

        return viewBinding.root
    }

    private val openAddMarket: (UiState.Market) -> Unit
        inline get() = {
            MarketDialog.newInstance(MarketDialog.EDIT_MARKET, it).show(
                childFragmentManager, MarketDialog.MARKET_DIALOG_TAG
            )
        }
}