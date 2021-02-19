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
import com.together.utils.viewBinding


class AboutFragment : BaseFragment(R.layout.fragment_about) {

    private val viewBinding: FragmentAboutBinding by viewBinding {
        FragmentAboutBinding.bind(
            requireView()
        )
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
        viewBinding.aboutDisclaimer.text = HtmlCompat.fromHtml(
            getString(R.string.developer), HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        viewBinding.btnSettings.setOnClickListener { showPopup() }
        viewBinding.backButton.setOnClickListener {
            if (flavour == "createFragment") {  //fixme  later use id reference directly
                findNavController().navigate(requireContext().getIntIdentity(flavour, "id"))
            } else {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun startCall() {
        val number = viewModel.sellerProfile._telephoneNumber
            .replace(" ", "").trim()
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
            Toast.makeText(
                requireContext(),
                "App rating ist leider nicht möglich",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        const val TAG = "AboutFragment"
    }

    private fun showPopup() {
        val popupMenu = PopupMenu(requireActivity(), viewBinding.btnSettings)
        popupMenu.menuInflater.inflate(R.menu.menu_setting_about, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.btn_show_licences -> {
                    MainMessagePipe.uiEvent.onNext(UiEvent.ShowLicense(requireContext()))
                    true
                }
                R.id.btn_rate_app -> {
                    launchMarket()
                    true
                }
                R.id.btn_call -> {
                    startCall()
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
