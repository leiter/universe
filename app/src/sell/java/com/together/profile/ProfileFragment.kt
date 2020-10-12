package com.together.profile


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.app.Dialogs
import com.together.base.BaseFragment
import com.together.base.UiState
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.concurrent.TimeUnit

class ProfileFragment : BaseFragment(), callMe {

    companion object {
        const val TAG = "ProfileFragment"
    }

    private val channel = PublishSubject.create<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        post_profile.setOnClickListener {
            startIt().zipWith(channel.firstOrError()).subscribeOn(Schedulers.io())
                .subscribe({
                Log.e("TTTTT", "Done sleeping")

            },{})
        }

        disposable.addAll(
//            post_profile.clicks().subscribe {
//
//            val i = viewModel.profile
//            val r = Result.SellerProfile(
//                displayName = i.displayName,
//                firstName = i.firstName,
//                lastName = i.lastName,
//                street = i.street,
//                houseNumber = i.houseNumber,
//                city = i.city,
//                zipcode = i.zipcode
//            )
//
//            Database.profile().setValue(r).getCompletable().subscribe({ success ->
//                if (success) {
//                    MainActivity.reStart(context!!)
//                } else {
//                    Toast.makeText(context!!, "went wrong", Toast.LENGTH_SHORT).show()
//                }
//            }, {
//                Toast.makeText(context!!, "EEEEEEEEEEEEeee", Toast.LENGTH_SHORT).show()
//
//            })
//        },

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
            },

            add_pickup_place.clicks().subscribe {
                Dialogs().show(requireFragmentManager(), "Frag")
            }
        )

        viewModel.markets.observe(viewLifecycleOwner, Observer {
            places_list.removeAllViews()
            val adapter = MarketAdapter(requireContext(), it, open)
            (0 until adapter.count).forEach { pos ->
                val item = adapter.getView(pos, null, places_list)
                places_list.addView(item)
            }
        })
    }

    val open: (UiState.Market) -> Unit
        inline get() = { Dialogs.newInstance(Dialogs.EDIT_MARKET, it).show(requireFragmentManager(), "Edit") }



    fun startIt () : Single<Unit> {
        call()
        return Single.just(Unit)

    }

    override fun call() {
        Thread.sleep((3000))
        channel.onNext("Has been called")
    }

}

interface callMe {
    fun call()
}
