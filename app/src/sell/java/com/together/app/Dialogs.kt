package com.together.app


import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.together.base.MainViewModel
import com.together.base.UiState
import kotlinx.android.synthetic.sell.fragment_dialogs.*


class Dialogs : DialogFragment() {

    private var modeType: Int? = null
    private lateinit var market: UiState.Market

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            modeType = it.getInt(ARG_PARAM1)
            if (modeType== EDIT_MARKET) {
                market = it.getParcelable(ARG_PARAM2)!!
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.together.R.layout.fragment_dialogs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup(begin)
        setup(end)
        add_pickup_place.setOnClickListener {
            val market = UiState.Market(
                place_name.text.toString(),
                street.text.toString(),
                house.text.toString(),
                zipcode.text.toString(),
                city.text.toString(),
                weekdays.text.toString(),
                begin.text.toString(),
                end.text.toString()
            )

            val items = viewModel.markets.value!!
            items.add(market)
            viewModel.markets.value = items

            dismiss()
        }
        if(modeType== EDIT_MARKET){
            place_name.setText(market.name)
            street.setText(market.street)
            house.setText(market.houseNumber)
            zipcode.setText(market.zipcode)
            city.setText(market.city)
            weekdays.setText(market.dayOfWeek)
            begin.setText(market.begin)
            end.setText(market.end)
        }

    }

    private fun setup(text: EditText) {
        text.setOnClickListener {
            text.setText("")
            TimePickerDialog(activity, object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    text.setText("%02d:%02d Uhr".format(hourOfDay, minute))
                }

            }, 0, 0, true).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window!!.setLayout(width, height)
        }
//        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    companion object {

        const val CREATE_MARKET = 0
        const val EDIT_MARKET = 1

        private const val ARG_PARAM1 = "operationMode"
        private const val ARG_PARAM2 = "marketToEdit"


        @JvmStatic
        fun newInstance(param1: Int = CREATE_MARKET, param2: UiState.Market? = null) =
            Dialogs().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }
}
