package com.together.app

import android.app.TimePickerDialog
import android.os.Bundle
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.together.base.UiState
import com.together.databinding.FragmentDialogsBinding
import com.together.profile.PickDayFragment
import com.together.profile.ProfileViewModel


class MarketDialog : DialogFragment() {

    private var modeType: Int? = null
    private lateinit var market: UiState.Market
    private lateinit var viewBinding: FragmentDialogsBinding

    private val viewModel: ProfileViewModel by viewModels({ requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            modeType = it.getInt(ARG_PARAM1)
            if (modeType == EDIT_MARKET) {
                market = it.getParcelable(ARG_PARAM2)!!
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

            addPickupPlace.setOnClickListener {
                val market = UiState.Market(
                    placeName.text.toString(),
                    street.text.toString(),
                    house.text.toString(),
                    zipCode.text.toString(),
                    city.text.toString(),
                    weekdays.text.toString(),
                    begin.text.toString(),
                    end.text.toString()
                )
                val items = viewModel.markets.value!!
                val s = items.indexOfFirst { it.id == market.id }
                if (s > -1) {
                    items.removeAt(s)
                    items.add(s, market)
                } else items.add(market)
                viewModel.markets.value = items
                dismiss()
            }
            if (modeType == EDIT_MARKET) {
                viewModel.profileLive.observe(viewLifecycleOwner, {
                    val m = it.marketList.find { it.id == market.id }!!
                    placeName.setText(m.name)
                    street.setText(m.street)
                    house.setText(m.houseNumber)
                    zipCode.setText(m.zipCode)
                    city.setText(m.city)
                    weekdays.setText(m.dayOfWeek)
                    begin.setText(m.begin)
                    end.setText(m.end)
                })

            }
        }
        return viewBinding.root
    }

    private fun setup(text: EditText) {
        text.setOnClickListener {
            text.setText("")
            TimePickerDialog(
                activity,
                { _, hourOfDay, minute ->
                    text.setText("%02d:%02d Uhr".format(hourOfDay, minute))
                },
                0, 0, true
            ).show()
        }
    }

    private fun showDayPicker(text: EditText) {
        text.setOnClickListener {
            PickDayFragment.newInstance(market)
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

    companion object {

        private const val CREATE_MARKET = 0
        const val EDIT_MARKET = 1

        private const val ARG_PARAM1 = "operationMode"
        private const val ARG_PARAM2 = "marketToEdit"

        const val MARKET_DIALOG_TAG = "MARKET_DIALOG_TAG"

        @JvmStatic
        fun newInstance(param1: Int = CREATE_MARKET, param2: UiState.Market? = null) =
            MarketDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }
}
