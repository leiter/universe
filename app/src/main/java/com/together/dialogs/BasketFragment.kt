package com.together.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.databinding.FragmentBasketBinding
import com.together.utils.setDrawable
import com.together.utils.toDateString
import kotlinx.android.synthetic.main.fragment_basket.*
import java.util.*
import kotlin.collections.ArrayList

class BasketFragment : DialogFragment() {

    private lateinit var viewBinding: FragmentBasketBinding

    private lateinit var adapter: BasketAdapter

    private lateinit var viewModel: MainViewModel
    private val calendar: Calendar = Calendar.getInstance()
    private var showingTimePicker: Boolean = false
    private lateinit var days: Array<Date>

    private val clickToDelete: (UiState.Article) -> Unit
        inline get() = { input ->
            val pos = adapter.data.indexOf(adapter.data.first { input._id == it._id })
            viewModel.basket.value?.remove(viewModel.basket.value!!.first { it._id == input._id })
            adapter.notifyItemRemoved(pos)
            viewBinding.basketSum.text = calculatePurchaseSum(viewModel.basket.value!!)
            MainMessagePipe.uiEvent.onNext(UiEvent.BasketMinusOne)
            if (viewModel.basket.value?.size == 0) dismiss()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    private fun showSetTime(show: Boolean) {
        val d = if (show) R.drawable.ic_appointment_time else R.drawable.ic_check
        requireActivity().setDrawable(d, viewBinding.btnChangeAppointmentTime)
    }

    private val timeClicker: (View) -> Unit = {
        showSetTime(showingTimePicker)
        if (showingTimePicker) {
            updatePickUptime(viewBinding.tpSetAppointment.currentHour,
                viewBinding.tpSetAppointment.currentMinute)
            viewBinding.tlDateContainer.visibility = View.VISIBLE
            viewBinding.tlMarketContainer.visibility = View.VISIBLE
            viewBinding.tpSetAppointment.visibility = View.INVISIBLE
            dialog!!.btn_cancel_appointment_time.visibility = View.GONE
        } else {
            viewBinding.tlDateContainer.visibility = View.INVISIBLE
            viewBinding.tlMarketContainer.visibility = View.INVISIBLE
            viewBinding.tpSetAppointment.visibility = View.VISIBLE
            dialog!!.btn_cancel_appointment_time.visibility = View.VISIBLE
        }
        showingTimePicker = !showingTimePicker
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewBinding.btnChangeAppointmentTime.setOnClickListener(timeClicker)
        viewBinding.btnSetAppointment.setOnClickListener{}
        viewBinding.btnHideAppointment.setOnClickListener { showFinalizeDate(false) }
        viewBinding.btnShowAppointment.setOnClickListener { showFinalizeDate(true) }
        viewBinding.btnCancelAppointmentTime.setOnClickListener {
            viewBinding.tpSetAppointment.visibility = View.INVISIBLE
            viewBinding.tlDateContainer.visibility = View.VISIBLE
            viewBinding.tlMarketContainer.visibility = View.VISIBLE
            it.visibility = View.INVISIBLE
            requireActivity().setDrawable(R.drawable.ic_appointment_time,
                viewBinding.btnChangeAppointmentTime)
            showingTimePicker = false
        }
        viewBinding.tpSetAppointment.setIs24HourView(true)
        setupMarketButtons()
        alterTimePicker()
        return viewBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        viewBinding = FragmentBasketBinding.inflate(LayoutInflater.from(requireContext()))

        val b = viewModel.basket.value!!
        adapter = BasketAdapter(b, clickToDelete)
        viewBinding.basketSum.text = calculatePurchaseSum(b)
        viewBinding.orderBasket.adapter = adapter
        viewBinding.orderBasket.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        return AlertDialog.Builder(requireActivity(), R.style.MyDialogTheme)
            .setView(viewBinding.root).create()
    }

    private fun alterTimePicker() {
        val defaultMarket = viewModel.sellerProfile.marketList[0]
        val i = (viewBinding.tpSetAppointment as FrameLayout).children.iterator().next()
        val hourPicker = (i.touchables[0].parent as NumberPicker)
        hourPicker.minValue = defaultMarket.begin.split(":")[0].toInt()
        hourPicker.maxValue = defaultMarket.end.split(":")[0].toInt() -1
    }

    private fun calculatePurchaseSum(list: MutableList<UiState.Article>): String {
        var sum = 0.0
        list.forEach { sum += it.amountCount * it.priceDigit }
        return "Gesamtpreis    %.2f€".format(sum)
    }

    private fun setupMarketButtons() {
        viewModel.sellerProfile.marketList.forEach {
            val m = viewBinding.tlMarketContainer.newTab()
            m.text = it.name
            viewBinding.tlMarketContainer.addTab(m)
        }
        showDates(Calendar.THURSDAY)
        viewBinding.tlMarketContainer.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val i = viewModel.sellerProfile.marketList[viewBinding.tlMarketContainer.selectedTabPosition]
                showDates(i.dayIndicator)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                val i = viewModel.sellerProfile.marketList[viewBinding.tlMarketContainer.selectedTabPosition]
                showDates(i.dayIndicator)
            }
        })
    }

    private fun updatePickUptime(hour: Int, minute: Int) {
        val newArray = ArrayList<Date>(3)
        val date =  viewBinding.tlDateContainer.selectedTabPosition
        days.forEachIndexed { index, day ->
            calendar.clear()
            calendar.time = day
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            newArray.add(index, calendar.time)
        }
        val i = viewModel.sellerProfile.marketList[viewBinding.tlMarketContainer.selectedTabPosition]
        showDates(i.dayIndicator, newArray.toTypedArray(),date)
    }

    private fun showFinalizeDate(show: Boolean) {

        if (!show) {
            viewBinding.appointmentContainer.visibility =  View.GONE
        } else {
            viewBinding.orderContainer.visibility = View.GONE
            viewBinding.appointmentContainer.visibility = View.VISIBLE
        }
        if (!show) {
            viewBinding.orderContainer.visibility = View.VISIBLE
        }
    }

    private fun showDates(targetDay: Int, newDays: Array<Date>? = null, selectPos: Int = -1) {
        days = newDays ?: getDays(targetDay)
        viewBinding.tlDateContainer.removeAllTabs()
        days.forEach {
            val f = viewBinding.tlDateContainer.newTab()
            f.text = it.toDateString()
            viewBinding.tlDateContainer.addTab(f)
        }
        if(selectPos > -1 && selectPos < viewBinding.tlDateContainer.tabCount) {
            viewBinding.tlDateContainer.getTabAt(selectPos)?.select()
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
