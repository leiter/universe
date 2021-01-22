package com.together.dialogs

import android.app.Dialog
import android.os.Build
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
import com.together.utils.alterPickUptime
import com.together.utils.getDays
import com.together.utils.toDateString
import kotlinx.android.synthetic.main.fragment_basket.*
import java.util.*

class BasketFragment : DialogFragment() {

    private lateinit var viewBinding: FragmentBasketBinding
    private lateinit var adapter: BasketAdapter
    private val viewModel: MainViewModel by viewModel()
    private var showingTimePicker: Boolean = false

    private val clickToDelete: (UiState.Article) -> Unit
        inline get() = { input ->
            val pos = adapter.data.indexOf(adapter.data.first { input._id == it._id })
            viewModel.basket.value?.remove(viewModel.basket.value!!.first { it._id == input._id })
            viewModel.resetAmountCount(input._id)
            adapter.notifyItemRemoved(pos)
            viewBinding.basketSum.text = calculatePurchaseSum(viewModel.basket.value!!)
            MainMessagePipe.uiEvent.onNext(UiEvent.BasketMinusOne)
            if (viewModel.basket.value?.size == 0) dismiss()
        }

    private fun showSetTime(show: Boolean) {
        val d = if (show) R.drawable.ic_appointment_time else R.drawable.ic_check
        viewBinding.btnChangeAppointmentTime.setImageResource(d)
    }

    private val timeClicker: (View) -> Unit = {
        showSetTime(showingTimePicker)
        if (showingTimePicker) {
            updatePickUptime()
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

        viewBinding.btnSendOrder.setOnClickListener { viewModel.sendOrder() }
        viewBinding.btnChangeAppointmentTime.setOnClickListener(timeClicker)
        viewBinding.btnHideAppointment.setOnClickListener { showFinalizeDate(false) }
        viewBinding.btnShowAppointment.setOnClickListener { showFinalizeDate(true) }
        viewBinding.btnCancelAppointmentTime.setOnClickListener {
            viewBinding.tpSetAppointment.visibility = View.INVISIBLE
            viewBinding.tlDateContainer.visibility = View.VISIBLE
            viewBinding.tlMarketContainer.visibility = View.VISIBLE
            it.visibility = View.INVISIBLE
            viewBinding.btnChangeAppointmentTime.setImageResource(R.drawable.ic_appointment_time)
            showingTimePicker = false
        }
        viewBinding.tpSetAppointment.setIs24HourView(true)
        viewBinding.btnSetAppointment.setOnClickListener {
            setPaceAndDateForOrder()
            showFinalizeDate(false)
        }

        return viewBinding.root
    }

    private fun setPaceAndDateForOrder() {
        val selectedIndex = viewBinding.tlMarketContainer.selectedTabPosition
        val date = viewModel.days[viewBinding.tlDateContainer.selectedTabPosition]
        val market = viewModel.sellerProfile.marketList[selectedIndex]
        viewModel.setTimeDateForOrder(market, date)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMarketButtons()
        setPaceAndDateForOrder()
        alterTimePicker()
        viewModel.dayText.observe(viewLifecycleOwner, {
            viewBinding.tvMarketDate.text = it
        })
        viewModel.marketText.observe(viewLifecycleOwner, {
            viewBinding.tvMarketName.text = it
        })
    }

    private fun alterTimePicker() {
        val defaultMarket = viewModel.sellerProfile.marketList[0]
        val i = (viewBinding.tpSetAppointment as FrameLayout).children.iterator().next()
        val hourPicker = (i.touchables[0].parent as NumberPicker)
        val minutePicker = (i.touchables[1].parent as NumberPicker)
        hourPicker.minValue = defaultMarket.begin.split(":")[0].toInt()
        hourPicker.maxValue = defaultMarket.end.split(":")[0].toInt() - 1
        minutePicker.minValue = 0
        minutePicker.maxValue = 3
        minutePicker.displayedValues = arrayOf("00", "15", "30", "45")
    }

    private fun calculatePurchaseSum(list: MutableList<UiState.Article>): String {
        var sum = 0.0
        list.forEach { sum += it.amountCount * it.priceDigit }
        return "Gesamtpreis    %.2f€".format(sum)
    }

    private fun setupMarketButtons() {
        val marketList = viewModel.sellerProfile.marketList
        marketList.forEach {
            val m = viewBinding.tlMarketContainer.newTab()
            m.text = it.name
            viewBinding.tlMarketContainer.addTab(m)
        }
        showDates(marketList[0])
        viewModel.marketIndex = 0
        viewModel.dateIndex = 0
        viewBinding.tlMarketContainer.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val market =
                    viewModel.sellerProfile.marketList[viewBinding.tlMarketContainer.selectedTabPosition]
                viewModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
                showDates(market)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                val market =
                    viewModel.sellerProfile.marketList[viewBinding.tlMarketContainer.selectedTabPosition]
                viewModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
                showDates(market)
            }
        })
        viewBinding.tlDateContainer.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.dateIndex = viewBinding.tlDateContainer.selectedTabPosition
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                viewModel.dateIndex = viewBinding.tlDateContainer.selectedTabPosition
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun updatePickUptime() {
        val (hour, minute) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Pair(
                viewBinding.tpSetAppointment.currentHour,
                viewBinding.tpSetAppointment.currentMinute * 15
            )
        } else {
            Pair(
                viewBinding.tpSetAppointment.hour,
                viewBinding.tpSetAppointment.minute * 15
            )
        }
        val newArray = alterPickUptime(viewModel.days, hour, minute)
        val date = viewBinding.tlDateContainer.selectedTabPosition
        val market = viewModel.sellerProfile.marketList[
                viewBinding.tlMarketContainer.selectedTabPosition]
        showDates(market, newArray.toTypedArray(), date)
    }

    private fun showFinalizeDate(show: Boolean) {
        if (!show) {
            viewBinding.appointmentContainer.visibility = View.GONE
        } else {
            viewBinding.orderContainer.visibility = View.GONE
            viewBinding.appointmentContainer.visibility = View.VISIBLE
        }
        if (!show) {
            viewBinding.orderContainer.visibility = View.VISIBLE
        }
    }

    private fun showDates(
        market: UiState.Market,
        newDays: Array<Date>? = null,
        selectPos: Int = -1
    ) {
        viewModel.days = newDays ?: getDays(market)
        viewBinding.tlDateContainer.removeAllTabs()
        viewModel.days.forEach {
            val f = viewBinding.tlDateContainer.newTab()
            f.text = it.toDateString()
            viewBinding.tlDateContainer.addTab(f)
        }
        if (selectPos > -1 && selectPos < viewBinding.tlDateContainer.tabCount) {
            viewBinding.tlDateContainer.getTabAt(selectPos)?.select()
            viewModel.dateIndex = selectPos
        }
    }
}
