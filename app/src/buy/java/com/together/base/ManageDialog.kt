package com.together.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.about.AboutFragment
import com.together.databinding.ManageDialogBinding
import com.together.loggedout.LoginFragment
import com.together.profile.ClientProfileFragment
import com.together.utils.createBasketUDate
import io.reactivex.disposables.Disposable


class ManageDialog : DialogFragment() {


    lateinit var disposable: Disposable
    private var vB: ManageDialogBinding? = null
    private val viewBinding:ManageDialogBinding  //by viewBinding(ManageDialogBinding::bind)
        get() = vB!!

    private lateinit var adapter: OldOrdersAdapter
    private val viewModel: MainViewModel by viewModels({ requireParentFragment() })


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())
        vB = ManageDialogBinding.inflate(LayoutInflater.from(requireContext()))
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
                ClientProfileFragment(), AboutFragment.TAG
            )
        )
        dismiss()
    }

    private val clickToOpenOrder: (UiState.Order) -> Unit = {
        viewModel.order = it
        val neList = viewModel.productList.value!!.toMutableSet().toList()
        viewModel.basket.value = createBasketUDate(neList, it.copy())
        BasketFragment().show(requireParentFragment().childFragmentManager, BasketFragment.TAG)
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adapter = OldOrdersAdapter(emptyList(), clickToOpenOrder)
        viewBinding.rvOldOrders.adapter = adapter
        viewBinding.rvOldOrders.layoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(
            viewBinding.rvOldOrders.context,
            LinearLayoutManager.VERTICAL
        )
        viewBinding.rvOldOrders.addItemDecoration(dividerItemDecoration)

        viewModel.loggedState.observe(viewLifecycleOwner, {

            when (it) {
                is UiState.LOGGEDOUT -> {
                    viewBinding.btnProfile.setOnClickListener(clickProfileWhileLoggedOut)
                    viewBinding.btnLogOut.setOnClickListener(clickProfileWhileLoggedOut)
                    viewBinding.btnLogOut.setText(R.string.login_btn_text)
                }

                is UiState.BaseAuth -> {
                    viewBinding.btnProfile.setOnClickListener(clickProfileWhileLoggedIn)
                    viewBinding.btnLogOut.setOnClickListener {
                        viewModel.clearAccount()
                    }
                    viewBinding.btnLogOut.setText(R.string.invalidate_session)
                }
            }
        })

        viewModel.blockingLoaderState.observe(viewLifecycleOwner, { uiEvent ->
            when (uiEvent) {
                is UiEvent.LoadingDone -> {
                    if (uiEvent.indicator == UiEvent.LOAD_OLD_ORDERS) {
                        viewBinding.prLoadOrders.visibility = View.GONE
                        viewModel.oldOrders.observe(viewLifecycleOwner, orderObserver)
                        viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral
                    }
                    if (uiEvent.indicator == UiEvent.CLEAR_ACCOUNT) {
                        viewBinding.prLogOut.visibility = View.GONE
                        viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral
                        dismiss()
                    }
                }
                is UiEvent.Loading -> {
                    if (uiEvent.indicator == UiEvent.LOAD_OLD_ORDERS) {
                        viewBinding.prLoadOrders.visibility = View.VISIBLE
                    }
                    if (uiEvent.indicator == UiEvent.CLEAR_ACCOUNT) {
                        viewBinding.prLogOut.visibility = View.VISIBLE
                    }
                }
                is UiEvent.LoadingNeutral -> {
                    viewModel.oldOrders.removeObserver(orderObserver)
                }
            }
        })

        viewBinding.btnShowOrders.setOnClickListener { viewModel.loadOrders() }

        viewBinding.btnWriteMsg.setOnClickListener { showWriteMessage(true) }
        viewBinding.btnCancel.setOnClickListener { showWriteMessage(false) }

        viewBinding.btnShowInfo.setOnClickListener {
            dismiss()
            MainMessagePipe.uiEvent.onNext(UiEvent.ShowLicense(requireContext()))
//            MainMessagePipe.uiEvent.onNext(
//                UiEvent.AddFragment(
//                    requireActivity().supportFragmentManager,
//                    AboutFragment(), AboutFragment.TAG
//                )
//            );
//            dismiss()
        }
        return viewBinding.root
    }

    private val orderObserver: (List<UiState.Order>) -> Unit = {
        if (it.isNotEmpty()) {
            showOldOrders()
            adapter.data = it
            adapter.notifyDataSetChanged()
        } else {
            Toast.makeText(
                requireContext(),
                "Bisher wurden bisher keine Bestellungen aufgegeben.", Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
    }

    private fun showWriteMessage(show: Boolean) {
        if (!show) {
            viewBinding.messageContainer.visibility = View.GONE
        }
        val visible = if (!show) View.VISIBLE else View.GONE
        showButtons(visible)
        if (show) {
            viewBinding.messageContainer.visibility = View.VISIBLE
        }
    }

    private fun showButtons(visible: Int) {
        viewBinding.btnProfile.visibility = visible
        viewBinding.btnWriteMsg.visibility = visible
        viewBinding.btnShowInfo.visibility = visible
        viewBinding.btnLogOut.visibility = visible
        viewBinding.btnShowOrders.visibility = visible
    }

    private fun showOldOrders() {
        viewBinding.rvOldOrders.visibility = View.VISIBLE
        showButtons(View.GONE)
    }

    override fun onDestroy() {
        vB = null
        disposable.dispose()
        super.onDestroy()
    }

    companion object {
        const val TAG = "ManageDialog"
    }
}

