package com.together.app

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.together.base.MainViewModel
import com.together.base.UiState
import com.together.databinding.FragmentDialogsBinding


class MarketDialog : DialogFragment() {

    private var modeType: Int? = null
    private lateinit var market: UiState.Market
    private lateinit var viewBinding: FragmentDialogsBinding

    val viewModel: MainViewModel by viewModels ({ requireParentFragment() })

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
        viewBinding = FragmentDialogsBinding.inflate(inflater,container,false)

        with(viewBinding){
            setup(begin)
            setup(end)

            addPickupPlace.setOnClickListener {
                val market = UiState.Market(
                    placeName.text.toString(),
                    street.text.toString(),
                    house.text.toString(),
                    zipcode.text.toString(),
                    city.text.toString(),
                    weekdays.text.toString(),
                    begin.text.toString(),
                    end.text.toString()
                )
                val items = viewModel.markets.value!!
                val s = items.indexOfFirst { it._id == market._id}
                if (s > -1) {
                    items.removeAt(s)
                    items.add(s, market)
                } else items.add(market)
                viewModel.markets.value = items
                dismiss()
            }
            if (modeType == EDIT_MARKET) {
                placeName.setText(market.name)
                street.setText(market.street)
                house.setText(market.houseNumber)
                zipcode.setText(market.zipCode)
                city.setText(market.city)
                weekdays.setText(market.dayOfWeek)
                begin.setText(market.begin)
                end.setText(market.end)
            }
        }
        return viewBinding.root
    }

    private fun setup(text: EditText) {
        text.setOnClickListener {
            text.setText("")
            TimePickerDialog(activity,
                { _, hourOfDay, minute ->
                    text.setText("%02d:%02d Uhr".format(hourOfDay, minute))
                },
                0, 0, true
            ).show()
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
