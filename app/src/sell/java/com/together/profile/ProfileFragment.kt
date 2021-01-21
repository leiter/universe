package com.together.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.jakewharton.picasso.OkHttp3Downloader
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.squareup.picasso.Picasso
import com.together.R
import com.together.app.Dialogs
import com.together.app.MainActivity
import com.together.base.*
import com.together.base.UiState.SellerProfile
import com.together.repository.Database
import com.together.repository.storage.getSingle
import kotlinx.android.synthetic.main.fragment_create.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.memberProperties

class ProfileFragment : BaseFragment() {

    companion object {
        const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        post_profile.setOnClickListener {
            val i = viewModel.profile
            if (!errorHints(i)) return@setOnClickListener
            val r = com.together.repository.Result.SellerProfile(
                displayName = i.displayName,
                firstName = i.firstName,
                lastName = i.lastName,
                street = i.street,
                houseNumber = i.houseNumber,
                city = i.city,
                zipcode = i.zipCode
            )

            Database.sellerProfile("",true).setValue(r).getSingle().subscribe({ success ->
                if (success) {
                    MainActivity.reStart(requireContext())
                } else {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(requireContext(), "EEEEEEEEEEEEeee", Toast.LENGTH_SHORT).show()
            })
        }

        phone_button.setOnClickListener {
            UtilsActivity.startAddImage(requireActivity())
        }

        viewModel.newProduct.observe(viewLifecycleOwner, Observer {
            Picasso.Builder(context).downloader(OkHttp3Downloader(context)).build()
                .load(it.uri).into(store_image)
        })

        disposable.addAll(

            city.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.city = it.toString()
            },
            zipcode.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.zipCode = it.toString()
            },
            house.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.houseNumber = it.toString()
            },
            street.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.street = it.toString()
            }
            ,
            company_name.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.displayName = it.toString()
            },
            first_name.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.firstName = it.toString()
            },
            last_name.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
                viewModel.profile.lastName = it.toString()
            },
            add_pickup_place.clicks().subscribe {
                Dialogs().show(requireActivity().supportFragmentManager, "Frag")
            }
        )

        viewModel.markets.observe(viewLifecycleOwner, Observer {
            places_list.removeAllViews()
            val adapter = MarketAdapter(requireContext(), it, openAddMarket)
            (0 until adapter.count).forEach { pos ->
                val item = adapter.getView(pos, null, places_list)
                places_list.addView(item)
            }
        })
    }

    private val openAddMarket: (UiState.Market) -> Unit
        inline get() = {
            Dialogs.newInstance(Dialogs.EDIT_MARKET, it).show(
                requireActivity().supportFragmentManager, "Edit"
            )
        }

    private fun errorHints(profile: SellerProfile): Boolean {
        val toBeChecked =
            SellerProfile::class.memberProperties.filter {
                !it.name.startsWith("_")
            }
        toBeChecked.forEach { prop ->
            val p = prop.get(profile) as String
            if (p.isEmpty()) {
                MainMessagePipe.uiEvent.onNext(
                    UiEvent.ShowToast(
                        requireContext(),
                        R.string.developer_error_hint
                    )
                )
                return false
            }
        }
        return true
    }

}