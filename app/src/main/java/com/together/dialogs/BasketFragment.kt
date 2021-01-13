package com.together.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.utils.toDateString
import java.util.*
import kotlin.collections.ArrayList

class BasketFragment : DialogFragment() {

    private lateinit var adapter: BasketAdapter
    private lateinit var dateContainer: TabLayout
    private lateinit var marketContainer: TabLayout
    private lateinit var timePicker: TimePicker
    private lateinit var timeSelector: AppCompatImageButton

    private lateinit var viewModel: MainViewModel
    private val calendar: Calendar = Calendar.getInstance()

    private var showingTimePicker : Boolean = false

    private val clickToDelete: (UiState.Article) -> Unit
        inline get() = { input ->
            val pos = adapter.data.indexOf(adapter.data.first { input._id == it._id })
            viewModel.basket.value?.remove(viewModel.basket.value!!.first { it._id == input._id })
            adapter.notifyItemRemoved(pos)
            val basketSum = dialog!!.findViewById<TextView>(R.id.basket_sum)
            basketSum.text = calculatePurchaseSum(viewModel.basket.value!!)
            MainMessagePipe.uiEvent.onNext(UiEvent.BasketMinusOne)
            if (viewModel.basket.value?.size == 0) dismiss()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    private val timeClicker: (View) -> Unit = { btn ->
        val d = if (showingTimePicker) ResourcesCompat.getDrawable(
            resources,R.drawable.ic_appointment_time,requireActivity().theme)
        else ResourcesCompat.getDrawable(resources,R.drawable.ic_add, requireActivity().theme)
        (btn as AppCompatImageButton).setImageDrawable(d)
        if(showingTimePicker){
            dateContainer.visibility = View.VISIBLE
            marketContainer.visibility = View.VISIBLE
            timePicker.visibility = View.INVISIBLE
            timePicker.currentHour
            timePicker.currentMinute

        } else {
            dateContainer.visibility = View.INVISIBLE
            marketContainer.visibility = View.INVISIBLE
            timePicker.visibility = View.VISIBLE
        }


        showingTimePicker = !showingTimePicker

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = requireActivity().layoutInflater.inflate(R.layout.fragment_basket, null)

        timeSelector = layout.findViewById(R.id.btn_change_appointment_time)
        timeSelector.setOnClickListener(timeClicker)
        dateContainer = layout.findViewById(R.id.tl_date_container)
        marketContainer = layout.findViewById(R.id.tl_market_select)
        timePicker = layout.findViewById(R.id.tp_set_appointment)
        timePicker.setIs24HourView(true)

        setupMarketButtons()

        val hansa = layout.findViewById<MaterialButton>(R.id.btn_hansa)
        hansa.setOnClickListener { showDates(Calendar.FRIDAY) }
        val uncle = layout.findViewById<MaterialButton>(R.id.btn_uncle)
        uncle.setOnClickListener { showDates(Calendar.THURSDAY) }


        val hideAppointment = layout.findViewById<ImageButton>(R.id.btn_hide_appointment)
        hideAppointment.setOnClickListener { showFinalizeDate(false) }

        val appointment = layout.findViewById<ImageButton>(R.id.btn_show_appointment)
        appointment.setOnClickListener { showFinalizeDate(true) }

        val b = viewModel.basket.value!!
        adapter = BasketAdapter(b, clickToDelete)
        layout.findViewById<TextView>(R.id.basket_sum).text = calculatePurchaseSum(b)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.order_basket)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        return AlertDialog.Builder(requireActivity(), R.style.MyDialogTheme).setView(layout)
            .create()
    }

    private fun calculatePurchaseSum(list: MutableList<UiState.Article>): String {
        var sum = 0.0
        list.forEach { sum += it.amountCount * it.priceDigit }
        return "Gesamtpreis    %.2f€".format(sum)
    }

    private fun setupMarketButtons() {
        viewModel.sellerProfile.marketList.forEach {
            val m = marketContainer.newTab()
            m.text = it.name
            marketContainer.addTab(m)
        }
        showDates(Calendar.THURSDAY)
        marketContainer.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val i = viewModel.sellerProfile.marketList[marketContainer.selectedTabPosition]
                showDates(i.dayIndicator)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                val i = viewModel.sellerProfile.marketList[marketContainer.selectedTabPosition]
                showDates(i.dayIndicator)
            }

        })
    }

    private fun showFinalizeDate(show: Boolean) {
        val d = dialog!!

        if (!show) {
            d.findViewById<ConstraintLayout>(R.id.appointment_container).visibility =
                if (show) View.VISIBLE else View.GONE
        } else {
            d.findViewById<RelativeLayout>(R.id.order_container).visibility = View.GONE
            d.findViewById<ConstraintLayout>(R.id.appointment_container).visibility =
                if (show) View.VISIBLE else View.GONE

        }
        if (!show) {
            d.findViewById<RelativeLayout>(R.id.order_container).visibility = View.VISIBLE
            d.findViewById<ConstraintLayout>(R.id.appointment_container).visibility =
                if (show) View.VISIBLE else View.GONE
            return
        }
    }


    private fun showDates(targetDay: Int) {
        val days = getDays(targetDay)
        dateContainer.removeAllTabs()
        days.forEach {
            val f = dateContainer.newTab()
            f.text = it.toDateString()
            dateContainer.addTab(f)
        }
    }

    private fun getDays(targetDay: Int): Array<Date> {
        calendar.time = Date()
        while (calendar.get(Calendar.DAY_OF_WEEK) != targetDay) {
            calendar.add(Calendar.DATE, 1)
        }
        val result = ArrayList<Date>(3)
        (0 until 3).forEach { index ->
            val plusDays = index * 7
            if (index != 0) calendar.add(Calendar.DATE, plusDays)
            result.add(calendar.time)
        }
        return result.toTypedArray()
    }

}
