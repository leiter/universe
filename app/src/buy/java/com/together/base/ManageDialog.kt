package com.together.dialogs

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.about.AboutFragment
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.databinding.ManageDialogBinding
import com.together.loggedout.LoginFragment
import com.together.splash.SplashScreenFragment
import io.reactivex.disposables.Disposable

class ManageDialog : DialogFragment() {

    lateinit var viewModel: MainViewModel
    lateinit var disposable: Disposable
    private lateinit var viewBinding: ManageDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())
        viewBinding = ManageDialogBinding.inflate(LayoutInflater.from(requireContext()))

        disposable = viewBinding.messageText.textChanges().subscribe {
            viewModel.smsMessageText = it.toString()
        }

        builder.setView(viewBinding.root)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    private val clickProfileWhileLoggedOut: (View) -> Unit = {
        MainMessagePipe.uiEvent.onNext(
            UiEvent.AddFragment(
                requireActivity().supportFragmentManager,
                LoginFragment(), LoginFragment.TAG
            )
        )
        dismiss()
    }

    private val clickProfileWhileLoggedIn: (View) -> Unit = {
        MainMessagePipe.uiEvent.onNext(
            UiEvent.AddFragment(
                requireActivity().supportFragmentManager,
                SplashScreenFragment(), AboutFragment.TAG
            )
        )
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.loggedState.observe(viewLifecycleOwner, {

            when (it) {
                is UiState.LOGGEDOUT -> {
                    viewBinding.btnProfile.setOnClickListener(clickProfileWhileLoggedOut)
                    viewBinding.btnLogOut.setOnClickListener(clickProfileWhileLoggedOut)
                    viewBinding.btnLogOut.setText(R.string.login_btn_text)
                }

                is UiState.BASE_AUTH -> {
                    viewBinding.btnProfile.setOnClickListener(clickProfileWhileLoggedIn)
                    viewBinding.btnLogOut.setOnClickListener {
                        MainMessagePipe.uiEvent.onNext(UiEvent.LogOut); dismiss() }
                    viewBinding.btnLogOut.setText(R.string.logout_btn_text)
                }
            }
        })

//        viewBinding.btnSendMessage.setOnClickListener { sendSms() }
        viewBinding.btnWriteMsg.setOnClickListener { showWriteMessage(true) }
        viewBinding.btnCancel.setOnClickListener { showWriteMessage(false) }

        viewBinding.btnShowInfo.setOnClickListener {
            MainMessagePipe.uiEvent.onNext(
                UiEvent.AddFragment(
                    requireActivity().supportFragmentManager,
                    AboutFragment(), AboutFragment.TAG
                )
            ); dismiss()
        }

        return viewBinding.root
    }

    private fun showWriteMessage(show: Boolean) {

        if (!show) { viewBinding.messageContainer.visibility = View.GONE }

        val visible = if (!show) View.VISIBLE else View.GONE
        viewBinding.btnProfile.visibility = visible
        viewBinding.btnWriteMsg.visibility = visible
        viewBinding.btnShowInfo.visibility = visible
        viewBinding.btnLogOut.visibility = visible

        if (show) { viewBinding.messageContainer.visibility = View.VISIBLE }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE_PERMISSION &&
//            permissions[0] == Manifest.permission.SEND_SMS &&
//            grantResults[0] == PackageManager.PERMISSION_GRANTED
//        ) {
//            sendSms()
//        }
//    }

//    private fun sendSms() {
//        when (ActivityCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.SEND_SMS
//        ) == PackageManager.PERMISSION_GRANTED) {
//            true -> {
//                if(viewModel.smsMessageText.isBlank()||
//                    viewModel.smsMessageText.isEmpty()){
//                    Toast.makeText(requireContext(),
//                        "Bitte text eingeben.",Toast.LENGTH_SHORT).show()
//                    return
//                }
//                val smsManager = SmsManager.getDefault()
//                val parts = smsManager.divideMessage(viewModel.smsMessageText)
//                smsManager.sendMultipartTextMessage(
//                    viewModel.sellerProfile._telephoneNumber, null,
//                    parts, null, null
//                )
//                dismiss()
//            }
//            else -> {
//                requestPermissions(
//                    arrayOf(Manifest.permission.SEND_SMS), REQUEST_CODE_PERMISSION)
//            }
//        }
//    }

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_CODE_PERMISSION = 10
        const val TAG = "ManageDialog"
    }
}

