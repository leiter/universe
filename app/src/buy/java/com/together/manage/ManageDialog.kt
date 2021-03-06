package com.together.manage

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.about.AboutFragment
import com.together.base.*
import com.together.base.UiEvent.Companion.CLEAR_ACCOUNT
import com.together.base.UiEvent.Companion.LOAD_OLD_ORDERS
import com.together.basket.BasketFragment
import com.together.databinding.ManageDialogBinding
import com.together.profile.ClientProfileFragment
import com.together.repository.NoInternetConnection
import com.together.utils.*
import io.reactivex.disposables.Disposable


class ManageDialog : DialogFragment() {

    private lateinit var disposable: Disposable
    private val viewBinding: ManageDialogBinding by viewBinding(ManageDialogBinding::inflate)
    private var adapter: OldOrdersAdapter? = null
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(viewBinding.root)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    private val clickProfileWhileLoggedIn: (View) -> Unit = {
        MainMessagePipe.uiEvent.onNext(
            UiEvent.ReplaceFragment(
                requireActivity().supportFragmentManager,
                ClientProfileFragment(), ClientProfileFragment.TAG
            )
        ); dismiss()
    }

    private val clickToOpenOrder: (UiState.Order) -> Unit = { selectedOrder ->
        viewModel.order = selectedOrder
        viewModel.marketIndex = viewModel.sellerProfile.marketList.indexOfFirst {
            it.id == viewModel.order.marketId
        }

        val dayTime = selectedOrder.pickUpDate.toDate()
        viewModel.days = getDays(viewModel.provideMarket(), dayTime)
        val neList = viewModel.productList.value!!.toMutableSet().toList()
        viewModel.basket.value = createBasketUDate(neList, selectedOrder.copy())
        dismiss()
        BasketFragment().show(requireParentFragment().childFragmentManager, BasketFragment.TAG)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        disposable = viewBinding.messageText.textChanges().subscribe {
            viewModel.smsMessageText = it.toString()
        }

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
                is UiState.BaseAuth -> {
                    viewBinding.btnProfile.setOnClickListener(clickProfileWhileLoggedIn)
                    viewBinding.btnLogOut.setOnClickListener {
                        viewModel.clearAccount()
                    }
                    viewBinding.btnLogOut.setText(R.string.invalidate_session)
                }
//                else -> Log.d(TAG, "Only interested logged in state.")
            }
        })

        viewModel.blockingLoaderState.observe(viewLifecycleOwner, { uiEvent ->
            when (uiEvent) {
                is UiEvent.LoadingDone -> {
                    if (uiEvent.contextId == LOAD_OLD_ORDERS) {
                        viewBinding.prLoadOrders.visibility = View.GONE
                        viewModel.oldOrders.observe(viewLifecycleOwner, orderObserver)
                        uiEvent.exception?.let {
                            when (uiEvent.exception) {
                                is NoInternetConnection ->
                                    requireContext().showLongToast("Keine Internetverbindung.")
                                else -> requireContext().showLongToast("Bestellungen konnten nicht geladen werden.")
                            }
                        }
                        viewModel.blockingLoaderState.value =
                            UiEvent.LoadingNeutral(LOAD_OLD_ORDERS)
                    }
                    if (uiEvent.contextId == CLEAR_ACCOUNT) {
                        viewBinding.prLogOut.visibility = View.GONE
                        viewModel.blockingLoaderState.value = UiEvent.LoadingNeutral(CLEAR_ACCOUNT)
                        dismiss()
                    }
                }
                is UiEvent.Loading -> {
                    if (uiEvent.contextId == LOAD_OLD_ORDERS) {
                        viewBinding.prLoadOrders.visibility = View.VISIBLE
                    }
                    if (uiEvent.contextId == CLEAR_ACCOUNT) {
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
            MainMessagePipe.uiEvent.onNext(
                UiEvent.AddFragment(
                    requireActivity().supportFragmentManager,
                    AboutFragment(), AboutFragment.TAG
                )
            )
            dismiss()
        }
        return viewBinding.root
    }

    private val orderObserver: (List<UiState.Order>) -> Unit = {
        if (it.isNotEmpty()) {
            showOldOrders()
            adapter?.data = it
            adapter?.notifyDataSetChanged()
        } else {
            viewModel.snacks.value =
                UiEvent.Snack(msg = R.string.toast_msg_manage_dialog_not_ordered_yet)
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
        with(viewBinding){
            btnProfile.visibility = visible
            btnWriteMsg.visibility = visible
            btnShowInfo.visibility = visible
            btnLogOut.visibility = visible
            btnShowOrders.visibility = visible
        }

    }

    private fun showOldOrders() {
        viewBinding.rvOldOrders.visibility = View.VISIBLE
        showButtons(View.GONE)
    }

    override fun onDestroyView() {
        adapter = null
        disposable.dispose()
        super.onDestroyView()
    }

    companion object {
        const val TAG = "ManageDialog"
    }

}

