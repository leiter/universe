package com.together.base

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.base.UiEvent.Companion.SEND_ORDER
import com.together.base.UiEvent.Companion.SEND_ORDER_FAILED
import com.together.base.UiEvent.Companion.SEND_ORDER_UPDATED
import com.together.databinding.FragmentBasketBinding
import com.together.utils.alterPickUptime
import com.together.utils.toDateString
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import viewBinding
import java.util.*

class BasketFragment : DialogFragment() {

    private val viewBinding: FragmentBasketBinding by viewBinding (FragmentBasketBinding::inflate)
    private var adapter: BasketAdapter? = null
    private val viewModel: MainViewModel by activityViewModels() //viewModels({ requireParentFragment() })
    private var showingTimePicker: Boolean = false
    private val disposable = CompositeDisposable()
    private val minuteSteps = 15

    private val clickToDelete: (UiState.Article) -> Unit
        inline get() = { input ->
            val ad = adapter!!
            val pos = ad.data.indexOf(ad.data.first { input.hashCode() == it.hashCode() })
            ad.data.removeAt(pos)
            viewModel.basket.value?.removeAt(pos)
            viewModel.resetAmountCount(input._id)
            ad.notifyItemRemoved(pos)
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
            viewBinding.btnCancelAppointmentTime.visibility = View.GONE
        } else {
            viewBinding.tlDateContainer.visibility = View.INVISIBLE
            viewBinding.tlMarketContainer.visibility = View.INVISIBLE
            viewBinding.tpSetAppointment.visibility = View.VISIBLE
            viewBinding.btnCancelAppointmentTime.visibility = View.VISIBLE
        }
        showingTimePicker = !showingTimePicker
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewBinding.btnSendOrder.setOnClickListener {
            if(viewBinding.ckSetReminder.isChecked){
//                viewModel.buyerProfile.
            }
            viewModel.sendOrder() }
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
        viewBinding.etMessage.setText(viewModel.order.message)
        return viewBinding.root
    }

    private fun setPaceAndDateForOrder() {
        val selectedIndex = viewBinding.tlMarketContainer.selectedTabPosition
        val date = viewModel.days[viewBinding.tlDateContainer.selectedTabPosition]
        val market = viewModel.sellerProfile.marketList[selectedIndex]
        viewModel.dateIndex = viewBinding.tlDateContainer.selectedTabPosition
        viewModel.marketIndex = selectedIndex
        viewModel.setTimeDateForOrder(market, date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = BasketAdapter(mutableListOf(), clickToDelete)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setListAdapter()
        return AlertDialog.Builder(requireActivity(), R.style.MyDialogTheme)
            .setView(viewBinding.root).create()
    }

    private fun setListAdapter(){
        val b = viewModel.basket.value!!.toMutableList()
        adapter?.data = b
        viewBinding.basketSum.text = calculatePurchaseSum(b)
        viewBinding.orderBasket.adapter = adapter
        viewBinding.orderBasket.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMarketButtons()
        setPaceAndDateForOrder()
        alterTimePicker()
        viewBinding.etMessage.textChanges().skipInitialValue().subscribe {
            viewModel.order.message = it.toString()
        }.addTo(disposable)
        viewModel.marketText.observe(viewLifecycleOwner, { viewBinding.tvMarketName.text = it })
        viewModel.dayText.observe(viewLifecycleOwner, { viewBinding.tvMarketDate.text = it })
        viewModel.blockingLoaderState.observe(viewLifecycleOwner, { uiEvent ->
            var toastMsg: String? = null
            when (uiEvent) {
                is UiEvent.LoadingDone -> {
                    when (uiEvent.indicator) {
                        SEND_ORDER -> {
                            viewBinding.progress.loadingIndicator.visibility = View.GONE
                            viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral
                            toastMsg = "Bestellung erfolgreich gesendet."
                            viewModel.basket.value = mutableListOf()
                            dismiss()
                        }
                        SEND_ORDER_FAILED -> {
                            toastMsg = "Bestellung konnte nicht gesendet werden."
                            viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral
                            dismiss()
                        }
                        SEND_ORDER_UPDATED -> {
                            toastMsg = "DiparentFragmente bereits bestellten Produkte wurde geladen, bitte überprüfen sie die Bestellung."
                            viewBinding.progress.loadingIndicator.visibility = View.GONE
                            viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral
                            setListAdapter()
                        }
                    }
                    Toast.makeText(
                        requireContext().applicationContext,
                        toastMsg, Toast.LENGTH_LONG
                    ).show()
                }

                is UiEvent.Loading -> {
                    if (uiEvent.indicator == SEND_ORDER) {
                        viewBinding.progress.loadingIndicator.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun alterTimePicker() {
        val defaultMarket = viewModel.sellerProfile.marketList[viewModel.marketIndex]
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
        showDates(marketList[viewModel.marketIndex])

        viewBinding.tlMarketContainer.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val market =
                    viewModel.sellerProfile.marketList[viewBinding.tlMarketContainer.selectedTabPosition]
                viewModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
                showDates(market, null, viewModel.dateIndex)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                val market =
                    viewModel.sellerProfile.marketList[viewBinding.tlMarketContainer.selectedTabPosition]
                viewModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
                viewBinding.tlDateContainer.getTabAt(viewModel.dateIndex)?.select()
                showDates(market, null, viewModel.dateIndex)
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
        viewBinding.tlMarketContainer.getTabAt(viewModel.marketIndex)?.select()
    }

    @Suppress("DEPRECATION")
    private fun updatePickUptime() {
        val (hour, minute) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Pair(
                viewBinding.tpSetAppointment.currentHour,
                viewBinding.tpSetAppointment.currentMinute * minuteSteps
            )
        } else {
            Pair(
                viewBinding.tpSetAppointment.hour,
                viewBinding.tpSetAppointment.minute * minuteSteps
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
        val dayTime = if (viewModel.order.pickUpDate != 0L) viewModel.order.pickUpDate else null
        newDays?.let {
            viewModel.days = it
        } //?: //getDays(market, dayTime?.let { Date(it) })
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

    override fun onDestroyView() {
        disposable.clear()
        adapter = null
        viewBinding.orderBasket.adapter = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "Basket"
    }
}
