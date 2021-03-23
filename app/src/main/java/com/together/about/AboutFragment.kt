package com.together.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import com.together.R
import com.together.base.BaseFragment
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.databinding.FragmentAboutBinding
import com.together.utils.getIntIdentity
import com.together.utils.showPopup
import com.together.utils.showSnackbar
import com.together.utils.viewBinding


class AboutFragment : BaseFragment(R.layout.fragment_about) {

    private val viewBinding: FragmentAboutBinding by viewBinding {
        FragmentAboutBinding.bind(requireView())
    }

    private var flavour: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flavour = it.getString("navigationId_back_btn") ?: ""
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.btnSettings.setOnClickListener { showPopup() }
        if (flavour != "createFragment") {
            val string = requireContext().getIntIdentity("impressum", "string")
            viewBinding.aboutDisclaimer.text = HtmlCompat.fromHtml(
                getString(string), HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        viewBinding.backButton.setOnClickListener {
            if (flavour == "createFragment") {
                findNavController().navigate(requireContext().getIntIdentity(flavour, "id"))
            } else {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun startCall() {
        val number = viewModel.sellerProfile._telephoneNumber.replace(" ", "").trim()
        val uri = "tel:$number"
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(uri)
        startActivity(intent)
    }

    private fun launchMarket() {
        val uri: Uri = Uri.parse("market://details?id=" + requireContext().packageName)
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            viewBinding.root.showSnackbar(UiEvent.Snack(msg = R.string.toast_cannot_rate_the_app))
        }
    }

    companion object {
        const val TAG = "AboutFragment"
    }

    private val showLicense = {MainMessagePipe.uiEvent.onNext(UiEvent.ShowLicense(requireContext()))}
    private val rateApp = {launchMarket()}
    private val startCall = {startCall()}

    private fun showPopup() {
        val m = hashMapOf(  R.id.btn_show_licences to showLicense,
                            R.id.btn_rate_app to rateApp ,
                            R.id.btn_call to startCall)
        requireContext().showPopup(viewBinding.btnSettings, m, R.menu.menu_setting_about )
    }

}
