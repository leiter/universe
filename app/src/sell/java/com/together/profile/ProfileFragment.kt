package com.together.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.app.MainActivity
import com.together.base.BaseFragment
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.storage.getCompletable
import kotlinx.android.synthetic.sell.fragment_profile.*
import java.util.concurrent.TimeUnit

class ProfileFragment : BaseFragment() {

    companion object {
        const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disposable.addAll(post_profile.clicks().subscribe {

            val i = viewModel.profile
            val r = Result.SellerProfile(
                displayName = i.displayName,
                firstName = i.firstName,
                lastName = i.lastName,
                street = i.street,
                houseNumber = i.houseNumber,
                city = i.city,
                zipcode = i.zipcode
            )

            Database.profile().setValue(r).getCompletable().subscribe({ success ->
                if (success) {
                    MainActivity.reStart(context!!)
                } else {
Toast.makeText(context!!,"went wrong",Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(context!!,"EEEEEEEEEEEEeee",Toast.LENGTH_SHORT).show()

            })
        },

        city.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
            viewModel.profile.city = it.toString()
        },

        zipcode.textChanges().debounce(400, TimeUnit.MILLISECONDS).subscribe {
            viewModel.profile.zipcode = it.toString()
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
        })
    }


}
