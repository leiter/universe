package com.together.profile

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.children
import com.google.android.material.tabs.TabLayout
import com.together.R
import com.together.base.BaseFragment
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.databinding.FragmentClientProfileBinding
import com.together.order.ProductsFragment
import com.together.utils.getTimePair
import com.together.utils.handleProgress
import com.together.utils.hideIme
import com.together.utils.viewLifecycleLazy


class ClientProfileFragment : BaseFragment(R.layout.fragment_client_profile) {

    private val viewBinding by viewLifecycleLazy { FragmentClientProfileBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        setupTextFields()
        alterTimePicker()
        viewModel.loadingContainer.observe(viewLifecycleOwner,{
            handleProgress(it.profileUpload,
                viewBinding.progress.loadingIndicator,
                R.string.toast_fail_no_internet_profile_upload,
                R.string.toast_fail_unknown_profile_upload,
                R.string.toast_success_profile_upload
            )
        })
    }

    private fun setupTextFields() {
        viewBinding.tvPhoneNumber.setText(viewModel.buyerProfile.phoneNumber)
        viewBinding.tvDisplayName.setText(viewModel.buyerProfile.displayName)
        viewBinding.tvEmailAddress.setText(viewModel.buyerProfile.emailAddress)
        if(viewModel.buyerProfile.defaultMarket!=""){
            viewBinding.tvMarketName.text = viewModel.sellerProfile.marketList
                .first {  it.id == viewModel.buyerProfile.defaultMarket }.name
        }
        viewBinding.tvPickupTime.text = if(viewModel.buyerProfile.defaultTime!="")
            viewModel.buyerProfile.getDefaultTimeDisplay() else ""
    }

    private fun setupClicks() {
        viewBinding.btnBack.setOnClickListener {
            MainMessagePipe.uiEvent.onNext(
                UiEvent.ReplaceFragment(
                    requireActivity().supportFragmentManager,
                    ProductsFragment(), ProductsFragment.TAG)
            )
        }
        viewBinding.btnClearDisplayName.setOnClickListener { viewBinding.tvDisplayName.setText("") }
        viewBinding.btnClearEmail.setOnClickListener { viewBinding.tvEmailAddress.setText("") }
        viewBinding.btnClearPhoneNumber.setOnClickListener { viewBinding.tvPhoneNumber.setText("") }
        viewBinding.btnOptions.setOnClickListener { showPopup() }
        viewBinding.btnChooseMarket.setOnClickListener { showPickMarket(true) }
        viewBinding.btnClearMarket.setOnClickListener { clearMarketSetting() }
        viewBinding.btnSetMarket.setOnClickListener { setMarketAsDefault() }
        viewBinding.btnSetPickupTime.setOnClickListener { setPickUpTime() }
        viewBinding.btnClearPickupTime.setOnClickListener { clearPickUptime() }
        viewBinding.btnChoosePickupTime.setOnClickListener { showChoosePickUptime() }
        viewBinding.btnSaveProfile.setOnClickListener { uploadBuyerProfile() }
    }

    private fun uploadBuyerProfile(){
        viewModel.buyerProfile.phoneNumber = viewBinding.tvPhoneNumber.text.toString()
        viewModel.buyerProfile.displayName = viewBinding.tvDisplayName.text.toString()
        viewModel.buyerProfile.emailAddress = viewBinding.tvEmailAddress.text.toString()
        viewModel.uploadBuyerProfile(false)
    }

    private fun clearPickUptime() {
        viewBinding.tvPickupTime.text = ""
        viewModel.buyerProfile.defaultTime = ""
        showTimePicker(false)
    }

    private fun setPickUpTime() {
        val timeBox = viewBinding.tpSetAppointment.getTimePair().toList()
        val time = "%02d:%02d".format(timeBox[0],timeBox[1])
        viewModel.buyerProfile.defaultTime = time
        viewBinding.tvPickupTime.text = viewModel.buyerProfile.getDefaultTimeDisplay()
        showTimePicker(false)
    }

    private fun showChoosePickUptime() {
        if (viewModel.buyerProfile.defaultMarket == "") {
            Toast.makeText(
                requireContext(), "Bitte erst den Marktplatz bestimmen.",
                Toast.LENGTH_LONG
            ).show()
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
        viewBinding.tvPickupTime.text = ""
        viewModel.buyerProfile.defaultTime = ""
        viewBinding.tvMarketName.text = ""
        viewModel.buyerProfile.defaultMarket = ""
    }

    private fun showPickMarket(show: Boolean) {
        setupMarketButtons()
        val v = if (show) View.VISIBLE else View.GONE
        viewBinding.tlMarketContainer.visibility = v
        viewBinding.btnClearMarket.visibility = v
        viewBinding.btnSetMarket.visibility = v
        if(show) viewBinding.root.hideIme()
    }


    private fun setupMarketButtons() {
        val marketList = viewModel.sellerProfile.marketList
        viewBinding.tlMarketContainer.removeAllTabs()
        marketList.forEach {
            val m = viewBinding.tlMarketContainer.newTab()
            m.text = it.name
            viewBinding.tlMarketContainer.addTab(m, false)
        }

        viewBinding.tlMarketContainer.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewBinding.tvMarketName.text = viewModel.sellerProfile.marketList[
                        viewBinding.tlMarketContainer.selectedTabPosition].name
                viewModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
//                viewModel.marketIndex = com.together.utils.viewBinding.tlMarketContainer.selectedTabPosition
            }
        })
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