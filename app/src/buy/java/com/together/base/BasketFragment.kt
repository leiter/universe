package com.together.base

import android.app.Dialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.sdsmdg.tastytoast.TastyToast
import com.together.R
import com.together.base.UiEvent.Companion.SEND_ORDER
import com.together.base.UiEvent.Companion.SEND_ORDER_FAILED
import com.together.base.UiEvent.Companion.SEND_ORDER_UPDATED
import com.together.databinding.FragmentBasketBinding
import com.together.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.*

class BasketFragment : DialogFragment() {

    private val viewBinding: FragmentBasketBinding by viewBinding(FragmentBasketBinding::inflate)
    private var adapter: BasketAdapter? = null
    private val viewModel: MainViewModel by activityViewModels()
    private var showingTimePicker: Boolean = false
    private val disposable = CompositeDisposable()

    private val clickToDelete: (UiState.Article) -> Unit
        inline get() = { input ->
            val ad = adapter!!
            val pos = ad.data.indexOf(ad.data.first { input.hashCode() == it.hashCode() })
            ad.data.removeAt(pos)
            viewModel.basket.value?.removeAt(pos)
            viewModel.resetAmountCount(input.id)
            ad.notifyItemRemoved(pos)
            viewBinding.basketSum.text = calculatePurchaseSum(viewModel.basket.value!!)
            MainMessagePipe.uiEvent.onNext(UiEvent.BasketMinusOne)
            if (viewModel.basket.value?.size == 0) {
                viewModel.basket.value = mutableListOf()
                viewModel.order = UiState.Order()
                dismiss()
            }
        }

    private fun showSetTime(show: Boolean) {
        val d = if (show) R.drawable.ic_appointment_time else R.drawable.ic_check
        viewBinding.btnChangeAppointmentTime.setImageResource(d)
    }

    private val timeClicker: (View) -> Unit = {
        showSetTime(showingTimePicker)
        if (showingTimePicker) {
            updatePickUptime()
            with(viewBinding) {

            }
            viewBinding.tlDateContainer.visibility = View.VISIBLE
            viewBinding.tlMarketContainer.visibility = View.VISIBLE
            viewBinding.tpSetAppointment.visibility = View.INVISIBLE
            viewBinding.etMessageLayout.visibility = View.VISIBLE
            viewBinding.btnCancelAppointmentTime.visibility = View.GONE
        } else {
            viewBinding.etMessageLayout.visibility = View.GONE
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

        viewBinding.btnSendOrder.setOnClickListener { manageSendOrder() }

        viewBinding.ckSetReminder.checkedChanges().subscribe { isChecked ->
            if (isChecked) {
                viewModel.buyerProfile.defaultMarket =
                    viewModel.provideMarket().id
                viewModel.buyerProfile.defaultTime = viewModel.days[0].getHourAndMinute()
                viewModel.uploadBuyerProfile(true)
            }
        }.addTo(disposable)

        viewBinding.btnChangeAppointmentTime.setOnClickListener(timeClicker)
        viewBinding.btnHideAppointment.setOnClickListener {
            viewBinding.root.hideIme()
            showFinalizeDate(false)
        }
        viewBinding.btnShowAppointment.setOnClickListener { showFinalizeDate(true) }
        viewBinding.btnCancelAppointmentTime.setOnClickListener {
            viewBinding.etMessageLayout.show()
            viewBinding.tpSetAppointment.hide()
            viewBinding.tlDateContainer.show()
            viewBinding.tlMarketContainer.show()
            it.hide()
            viewBinding.btnChangeAppointmentTime.setImageResource(R.drawable.ic_appointment_time)
            showingTimePicker = false
        }
        viewBinding.tpSetAppointment.setIs24HourView(true)
        viewBinding.btnSetAppointment.setOnClickListener {
            setPaceAndDateForOrder()
            viewBinding.root.hideIme()
            showFinalizeDate(false)
        }
        viewBinding.etMessage.setText(viewModel.order.message)
        return viewBinding.root
    }

    private fun manageSendOrder() {
        if (viewBinding.tvClientName.text.toString().doesNotFit()) {
            viewBinding.tvClientNameLayout.error = "Eingabe ist erforderlich."
            viewBinding.tvClientName.textChanges().skipInitialValue().subscribe {
                if (it.toString().doesNotFit().not()) {
                    viewBinding.tvClientNameLayout.error = ""
                }
            }.addTo(disposable)
            return
        } else {
            viewModel.buyerProfile.displayName = viewBinding.tvClientName.text.toString()
        }
        viewModel.sendOrder()

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

    private fun setListAdapter() {
        val b = viewModel.basket.value!!.toMutableList()
        adapter?.data = b
        viewBinding.basketSum.text = calculatePurchaseSum(b)
        viewBinding.orderBasket.adapter = adapter
        viewBinding.orderBasket.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val displayCount = requireContext().getQuantityString(R.plurals.tv_basket_product_count,b.size,b.size)
        viewBinding.tvProductCount.text= displayCount
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMarketButtons()
        setPaceAndDateForOrder()
        alterTimePicker()

        viewBinding.tvClientName.setText(viewModel.buyerProfile.displayName)
        viewBinding.tvClientName.textChanges().skipInitialValue().subscribe {
            viewModel.order.buyerProfile.displayName = it.toString()
        }.addTo(disposable)
        viewBinding.etMessage.textChanges().skipInitialValue().subscribe {
            viewModel.order.message = it.toString()
        }.addTo(disposable)
        viewModel.marketText.observe(viewLifecycleOwner, { viewBinding.tvMarketName.text = it })
        viewModel.dayText.observe(viewLifecycleOwner, { viewBinding.tvMarketDate.text = it })
        viewModel.blockingLoaderState.observe(viewLifecycleOwner, { uiEvent ->
            var toastMsg: Int? = null
            var toastType: Int = -1
            when (uiEvent) {
                is UiEvent.LoadingDone -> {
                    when (uiEvent.contextId) {
                        SEND_ORDER -> {
                            viewBinding.progressss.loadingIndicator.remove()
                            viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral(SEND_ORDER)
                            toastMsg = R.string.toast_msg_send_success
                            viewModel.basket.value = mutableListOf()
                            viewModel.resetProductList()
                            viewModel.snacks.value = UiEvent.Snack(msg = toastMsg, backGroundColor = R.color.fab_green)
                            dismiss()
                        }
                        SEND_ORDER_FAILED -> {
                            toastMsg = R.string.toast_msg_could_not_send_order
                            viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral(
                                SEND_ORDER_FAILED
                            )
                            viewModel.snacks.value = UiEvent.Snack(msg = toastMsg,
                                backGroundColor = R.color.design_default_color_error)
                            dismiss()
                        }
                        SEND_ORDER_UPDATED -> {
                            toastMsg = R.string.toast_msg_already_ordered
                            viewBinding.progressss.loadingIndicator.remove()
                            viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral(
                                SEND_ORDER_UPDATED
                            )
                            Toast.makeText(
                                requireContext(),
                                toastMsg, Toast.LENGTH_LONG
                            ).show()
                            setListAdapter()
                        }
                    }
//                    if ((uiEvent.contextId == SEND_ORDER) || (uiEvent.contextId == SEND_ORDER_FAILED)){
//                        toastMsg?.let { viewModel.snacks.value = UiEvent.Snack(msg = toastMsg) }
//                        dismiss()
//                    }
                }

                is UiEvent.Loading -> {
                    if (uiEvent.contextId == SEND_ORDER) {
                        viewBinding.progressss.loadingIndicator.show()
                    }
                }
            }
        })

    }

    private fun alterTimePicker() {
        val defaultMarket = viewModel.provideMarket()
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
        val summ = "%.2f€".format(sum).replace(".", ",")
        return "Gesamtpreis    $summ"
    }

    private fun setupMarketButtons() {
        val marketList = viewModel.sellerProfile.marketList
        marketList.forEach {
            val m = viewBinding.tlMarketContainer.newTab()
            m.text = it.name
            viewBinding.tlMarketContainer.addTab(m)
        }
        showDates()

        viewBinding.tlMarketContainer.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition

                showDates(null, viewModel.dateIndex)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                viewModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
                viewBinding.tlDateContainer.getTabAt(viewModel.dateIndex)?.select()
                showDates(null, viewModel.dateIndex)
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

    private fun updatePickUptime() {
        val (hour, minute) = viewBinding.tpSetAppointment.getTimePair()
        val newArray = alterPickUptime(viewModel.days, hour, minute)
        val date = viewBinding.tlDateContainer.selectedTabPosition
        showDates(newArray.toTypedArray(), date)
    }

    private fun showFinalizeDate(show: Boolean) {
        if (!show) {
            viewBinding.appointmentContainer.remove()
        } else {
            viewBinding.orderContainer.remove()
            viewBinding.appointmentContainer.show()
            viewBinding.root.hideIme()
        }
        if (!show) {
            viewBinding.orderContainer.show()
        }
    }

    private fun showDates(
        newDays: Array<Date>? = null,
        selectPos: Int = -1
    ) {
        val dayTime = if (viewModel.order.pickUpDate != 0L) viewModel.order.pickUpDate
        else {
            if (viewModel.buyerProfile.defaultTime != "") {
                viewModel.setMarketAndTime()
            }
            viewModel.days[0].time
        }
        viewModel.days = newDays ?: getDays(
            viewModel.provideMarket(), Date(dayTime)
        )

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
