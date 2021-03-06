package com.together.app

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.base.UiState
import com.together.databinding.FragmentDialogsBinding
import com.together.profile.PickDayFragment
import com.together.profile.ProfileViewModel
import com.together.utils.showLongToast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class MarketDialog : DialogFragment() {

    private val disposable = CompositeDisposable()

    private var modeType: Int? = null
    private lateinit var market: UiState.Market
    private var marketIndex: Int = -1
    private lateinit var viewBinding: FragmentDialogsBinding

    private val viewModel: ProfileViewModel by viewModels({ requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            modeType = it.getInt(ARG_PARAM1)
            if (modeType == EDIT_MARKET) {
                marketIndex = it.getInt(ARG_PARAM3)
                market = viewModel.currentMarket
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentDialogsBinding.inflate(inflater, container, false)

        with(viewBinding) {
            setup(begin)
            setup(end)
            showDayPicker(weekdays)

            placeName.textChanges().skipInitialValue()
                .subscribe { market.name = it.toString() }.addTo(disposable)
            street.textChanges().skipInitialValue()
                .subscribe { market.street = it.toString() }.addTo(disposable)
            house.textChanges().skipInitialValue()
                .subscribe { market.houseNumber = it.toString() }.addTo(disposable)
            zipCode.textChanges().skipInitialValue()
                .subscribe { market.zipCode = it.toString() }.addTo(disposable)
            city.textChanges().skipInitialValue()
                .subscribe { market.city = it.toString() }.addTo(disposable)
            begin.textChanges().skipInitialValue()
                .subscribe { market.begin = it.toString() }.addTo(disposable)
            end.textChanges().skipInitialValue()
                .subscribe { market.end = it.toString() }.addTo(disposable)

            viewModel.profileLive.observe(viewLifecycleOwner, {
                weekdays.setText(viewModel.currentMarket.dayOfWeek)
            })

            addPickupPlace.setOnClickListener {
                viewModel.fillInMarket()
                when {
                    viewModel.currentMarket.isItGood().not() -> {
                        requireContext().showLongToast("Alle Felder sind auszufüllen.")
                    }
                    viewModel.currentMarket.isGoodTiming().not() -> {
                        requireContext().showLongToast("Der Beginn ist später als das Ende.")
                    }
                    else -> dismiss()
                }
            }
            placeName.setText(market.name)
            street.setText(market.street)
            house.setText(market.houseNumber)
            zipCode.setText(market.zipCode)
            city.setText(market.city)
            weekdays.setText(market.dayOfWeek)
            begin.setText(market.begin)
            end.setText(market.end)
        }
        return viewBinding.root
    }


    private fun setup(text: EditText) {
        text.setOnClickListener {
            text.setText("")
            TimePickerDialog(
                activity,
                R.style.SpinnerTimePicker,
                { _, hourOfDay, minute ->
                    val time = "%02d:%02d Uhr".format(hourOfDay, minute)
                    if(text.id== R.id.begin)viewModel.currentMarket.begin = time
                    if(text.id== R.id.end)viewModel.currentMarket.end = time
                    text.setText(time)
                },
                0, 0, true
            ).show()
        }
    }

    private fun showDayPicker(text: EditText) {
        text.setOnClickListener {
            viewModel.fillInMarket()
            PickDayFragment.newInstance(marketIndex)
                .show(parentFragmentManager, "PickDayFragment")
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window!!.setLayout(width, height)
        } //        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }


    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }

    companion object {

        private const val CREATE_MARKET = 0
        const val EDIT_MARKET = 1

        private const val ARG_PARAM1 = "operationMode"
        private const val ARG_PARAM3 = "marketToEdit"

        const val MARKET_DIALOG_TAG = "MARKET_DIALOG_TAG"

        @JvmStatic
        fun newInstance(param1: Int = CREATE_MARKET, pos: Int) =
            MarketDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putInt(ARG_PARAM3, pos)

                }
            }
    }
}
