package com.together.dialogs

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
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
import com.together.repository.Result
import com.together.splash.SplashScreenFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.manage_dialog.view.*

class ManageDialog : DialogFragment() {

    lateinit var viewModel: MainViewModel
    lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view =
            requireActivity().layoutInflater.inflate(R.layout.manage_dialog, null as ViewGroup?)
        view.btn_profile.setOnClickListener {
            MainMessagePipe.uiEvent.onNext(UiEvent.AddFragment(
                requireActivity().supportFragmentManager,
                SplashScreenFragment(), AboutFragment.TAG))

            ; dismiss() }
        view.btn_write_msg.setOnClickListener { showWriteMessage(true) }
        view.btn_show_info.setOnClickListener {
            MainMessagePipe.uiEvent.onNext(
                UiEvent.AddFragment(
                    requireActivity().supportFragmentManager,
                    AboutFragment(), AboutFragment.TAG
                )
            ); dismiss()
        }
        view.btn_log_out.setOnClickListener { MainMessagePipe.uiEvent.onNext(UiEvent.LogOut); dismiss() }
        view.btn_cancel.setOnClickListener { showWriteMessage(false) }
        disposable = view.message_text.textChanges().subscribe {
            viewModel.smsMessageText = it.toString()
        }
        view.btn_send_message.setOnClickListener {
            sendSms()
        }
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    private fun showWriteMessage(show: Boolean) {
        val d = dialog!!

        if (!show) {
            d.findViewById<RelativeLayout>(R.id.message_container).visibility =
                if (show) View.VISIBLE else View.GONE
        }

        val visible = if (!show) View.VISIBLE else View.GONE
        d.findViewById<TextView>(R.id.btn_profile).visibility = visible
        d.findViewById<TextView>(R.id.btn_write_msg).visibility = visible
        d.findViewById<TextView>(R.id.btn_show_info).visibility = visible
        d.findViewById<TextView>(R.id.btn_log_out).visibility = visible

        if (!show) { return }

        d.findViewById<RelativeLayout>(R.id.message_container).visibility =
            if (show) View.VISIBLE else View.GONE

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== REQUEST_CODE_PERMISSION &&
            permissions[0]==Manifest.permission.SEND_SMS  &&
            grantResults[0] ==PackageManager.PERMISSION_GRANTED){
            sendSms()
            dismiss()
        }
    }

    private fun sendSms(){
        when(ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            true -> {
                val smsManager = SmsManager.getDefault()
                val parts = smsManager.divideMessage(viewModel.smsMessageText)
                smsManager.sendMultipartTextMessage(viewModel.telephoneNumber, null,
                    parts,null, null)
                dismiss()
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.SEND_SMS), REQUEST_CODE_PERMISSION)
            }
        }
    }

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_CODE_PERMISSION = 10
    }

}