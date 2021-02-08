package com.together.profile

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.together.R
import com.together.base.BaseFragment
import com.together.base.MainViewModel
import com.together.databinding.FragmentClientProfileBinding
import com.together.utils.hideIme
import viewLifecycleLazy


class ClientProfileFragment : BaseFragment(R.layout.fragment_client_profile) {

    private val viewBinding by viewLifecycleLazy { FragmentClientProfileBinding.bind(requireView()) }

    private lateinit var vModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setupClicks()
        setupTextFields()
        alterTimePicker()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun setupTextFields() {
        viewBinding.tvPhoneNumber.setText(viewModel.buyerProfile.phoneNumber)
        viewBinding.tvDisplayName.setText(viewModel.buyerProfile.displayName)
        viewBinding.tvEmailAddress.setText(viewModel.buyerProfile.emailAddress)
    }

    private fun setupClicks(){
        viewBinding.btnBack.setOnClickListener { activity?.onBackPressed() }
        viewBinding.btnClearDisplayName.setOnClickListener { viewBinding.tvDisplayName.setText("") }
        viewBinding.btnClearEmail.setOnClickListener { viewBinding.tvEmailAddress.setText("") }
        viewBinding.btnClearPhoneNumber.setOnClickListener { viewBinding.tvPhoneNumber.setText("") }
        viewBinding.btnOptions.setOnClickListener { showPopup() }
        viewBinding.btnChooseMarket.setOnClickListener{ showPickMarket(true)}
        viewBinding.btnClearMarket.setOnClickListener { clearMarketSetting() }
        viewBinding.btnSetMarket.setOnClickListener { setMarketAsDefault() }
        viewBinding.btnChoosePickupTime.setOnClickListener { showChoosePickuptime()}
        viewBinding.btnSaveProfile.setOnClickListener {  }
    }

    private fun showChoosePickuptime() {
        if(vModel.buyerProfile.defaultMarket == ""){
            Toast.makeText(requireContext(),"Bitte erst den Marktplatz bestimmen.",
                Toast.LENGTH_LONG).show()
            return
        }
        showTimePicker(true)
    }

    private fun showTimePicker(show: Boolean){
        val v = if(show) View.VISIBLE else View.GONE
        viewBinding.tpSetAppointment.visibility = v
    }

    private fun setMarketAsDefault() {
        showPickMarket(false)
        vModel.buyerProfile.defaultMarket = vModel.sellerProfile
            .marketList[vModel.marketIndex]._id
    }

    private fun clearMarketSetting() {
        showPickMarket(false)
        viewBinding.tvMarketName.text = ""
    }

    private fun showPickMarket(show: Boolean) {
        setupMarketButtons()
        val v = if(show) View.VISIBLE else View.GONE
        viewBinding.tlMarketContainer.visibility = v
        viewBinding.btnClearMarket.visibility = v
        viewBinding.btnSetMarket.visibility = v
    }



    private fun setupMarketButtons() {
        val marketList = vModel.sellerProfile.marketList
        viewBinding.tlMarketContainer.removeAllTabs()
        marketList.forEach {
            val m = viewBinding.tlMarketContainer.newTab()
            m.text = it.name
            viewBinding.tlMarketContainer.addTab(m,false)
        }

        viewBinding.tlMarketContainer.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewBinding.tvMarketName.text = vModel.sellerProfile.marketList[
                        viewBinding.tlMarketContainer.selectedTabPosition].name
                vModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
//                vModel.marketIndex = viewBinding.tlMarketContainer.selectedTabPosition
            }
        })
    }

    private fun alterTimePicker() {
        viewBinding.tpSetAppointment.setIs24HourView(true)
        val defaultMarket = vModel.sellerProfile.marketList[vModel.marketIndex]
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

    override fun onDestroyView() {
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onDestroyView()
    }

    private fun showPopup() {
        val popupMenu = PopupMenu(requireActivity(), viewBinding.btnOptions)
        popupMenu.menuInflater.inflate(R.menu.menu_setting_client_profile,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.btn_clear_data -> {
//                    MainMessagePipe.uiEvent.onNext(UiEvent.ShowLicense(requireContext()))
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