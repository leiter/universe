package com.together.profile

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.children
import com.google.android.material.tabs.TabLayout
import com.together.R
import com.together.base.BaseFragment
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.databinding.FragmentClientProfileBinding
import com.together.order.ProductsFragment
import com.together.utils.*


class ClientProfileFragment : BaseFragment(R.layout.fragment_client_profile) {

    private val viewBinding by viewLifecycleLazy { FragmentClientProfileBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks(); setupTextFields(); alterTimePicker()
        viewModel.loadingContainer.observe(viewLifecycleOwner, {
            viewBinding.progress.loadingIndicator.visibility = it.profileUpload.visible
            handleProgress(
                it.profileUpload,
                R.string.toast_fail_no_internet_profile_upload,
                R.string.toast_fail_unknown_profile_upload,
                R.string.toast_success_profile_upload
            )
        })
    }

    private fun setupTextFields() {
        with(viewBinding) {
            tvPhoneNumber.setText(viewModel.buyerProfile.phoneNumber)
            tvDisplayName.setText(viewModel.buyerProfile.displayName)
            tvEmailAddress.setText(viewModel.buyerProfile.emailAddress)
            if (viewModel.buyerProfile.defaultMarket != "") {
                tvMarketName.text = viewModel.sellerProfile.marketList
                    .first { it.id == viewModel.buyerProfile.defaultMarket }.name
            }
            tvPickupTime.text = if (viewModel.buyerProfile.defaultTime != "")
                viewModel.buyerProfile.getDefaultTimeDisplay() else ""
        }
    }

    private fun setupClicks() {
        with(viewBinding) {
            btnBack.setOnClickListener {
                MainMessagePipe.uiEvent.onNext(
                    UiEvent.ReplaceFragment(
                        requireActivity().supportFragmentManager,
                        ProductsFragment(), ProductsFragment.TAG
                    )
                )
            }
            btnClearDisplayName.setOnClickListener { viewBinding.tvDisplayName.setText("") }
            btnClearEmail.setOnClickListener { viewBinding.tvEmailAddress.setText("") }
            btnClearPhoneNumber.setOnClickListener { viewBinding.tvPhoneNumber.setText("") }
            btnOptions.setOnClickListener { showPopup() }
            btnChooseMarket.setOnClickListener { showPickMarket(true) }
            btnClearMarket.setOnClickListener { clearMarketSetting() }
            btnSetMarket.setOnClickListener { setMarketAsDefault() }
            btnSetPickupTime.setOnClickListener { setPickUpTime() }
            btnClearPickupTime.setOnClickListener { clearPickUptime() }
            btnChoosePickupTime.setOnClickListener { showChoosePickUptime() }
            btnSaveProfile.setOnClickListener { uploadBuyerProfile() }
        }
    }

    private fun uploadBuyerProfile() {
        with(viewModel) {
            buyerProfile.phoneNumber = viewBinding.tvPhoneNumber.text.toString()
            buyerProfile.displayName = viewBinding.tvDisplayName.text.toString()
            buyerProfile.emailAddress = viewBinding.tvEmailAddress.text.toString()
            uploadBuyerProfile(false)
        }
    }

    private fun clearPickUptime() {
        viewBinding.tvPickupTime.text = ""
        viewModel.buyerProfile.defaultTime = ""
        showTimePicker(false)
    }

    private fun setPickUpTime() {
        val timeBox = viewBinding.tpSetAppointment.getTimePair().toList()
        val time = "%02d:%02d".format(timeBox[0], timeBox[1])
        viewModel.buyerProfile.defaultTime = time
        viewBinding.tvPickupTime.text = viewModel.buyerProfile.getDefaultTimeDisplay()
        showTimePicker(false)
    }

    private fun showChoosePickUptime() {
        if (viewModel.buyerProfile.defaultMarket == "") {
            viewModel.snacks.value = UiEvent.Snack(
                msg =
                R.string.toast_msg_client_profile_determin_market,
            )
            return
        }
        viewBinding.root.hideIme()
        showTimePicker(true)
    }

    private fun showTimePicker(show: Boolean) {
        val v = if (show) View.VISIBLE else View.GONE
        viewBinding.tpSetAppointment.visibility = v
        viewBinding.btnClearPickupTime.visibility = v
        viewBinding.btnSetPickupTime.visibility = v
    }

    private fun setMarketAsDefault() {
        showPickMarket(false)
        viewModel.buyerProfile.defaultMarket = viewModel.sellerProfile
            .marketList[viewModel.marketIndex].id
    }

    private fun clearMarketSetting() {
        showPickMarket(false)
        showTimePicker(false)
        viewBinding.tvPickupTime.text = ""
        viewModel.buyerProfile.defaultTime = ""
        viewBinding.tvMarketName.text = ""
        viewModel.buyerProfile.defaultMarket = ""
    }

    private fun showPickMarket(show: Boolean) {
        setupMarketButtons()
        if (show) {
            with(viewBinding) {
                tlMarketContainer.show()
                btnClearMarket.show()
                btnSetMarket.show()
            }
        } else {
            with(viewBinding) {
                tlMarketContainer.removeAllTabs()
                tlMarketContainer.remove()
                btnClearMarket.remove()
                btnSetMarket.remove()
            }
        }
        if (show) viewBinding.root.hideIme()
    }

    private fun setupMarketButtons() {

        val marketList = viewModel.sellerProfile.marketList

        viewBinding.tlMarketContainer.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewBinding.tvMarketName.text = viewModel.sellerProfile.marketList[
                        viewBinding.tlMarketContainer.selectedTabPosition].name
                viewModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewBinding.tlMarketContainer.removeAllTabs()
        marketList.forEach {
            val m = viewBinding.tlMarketContainer.newTab()
            m.text = it.name
            viewBinding.tlMarketContainer.addTab(m, false)
        }
    }

    private fun alterTimePicker() {
        viewBinding.tpSetAppointment.setIs24HourView(true)
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

    companion object {
        const val TAG = "ClientProfileFragment"
    }

    override fun onPause() {
        viewBinding.root.clearFocus()
        viewBinding.root.hideIme()
        super.onPause()
    }

    private fun showPopup() {
        val popupMenu = PopupMenu(requireActivity(), viewBinding.btnOptions)
        popupMenu.menuInflater.inflate(R.menu.menu_setting_client_profile, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.btn_clear_data -> {
                    viewModel.clearAccount()
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }
        popupMenu.show()
    }
}