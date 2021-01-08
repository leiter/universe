package com.together.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState

class BasketFragment : DialogFragment() {

    private lateinit var adapter: BasketAdapter

    lateinit var viewModel: MainViewModel

    private val clickToDelete: (UiState.Article) -> Unit
        inline get() = { input ->
            val pos = adapter.data.indexOf(adapter.data.first { input._id == it._id })
            viewModel.basket.value?.remove(viewModel.basket.value!!.first { it._id == input._id })
            adapter.notifyItemRemoved(pos)
            MainMessagePipe.uiEvent.onNext(UiEvent.BasketMinusOne)
            if (viewModel.basket.value?.size == 0) dismiss()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = requireActivity().layoutInflater.inflate(R.layout.fragment_basket, null)
        val b = viewModel.basket.value!!
        adapter = BasketAdapter(b, clickToDelete)
        layout.findViewById<TextView>(R.id.basket_sum).text = calculatePurchaseSum(b)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.order_basket)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        return AlertDialog.Builder(requireActivity(),R.style.MyDialogTheme).setView(layout).create()
    }

    private fun calculatePurchaseSum(list:MutableList<UiState.Article>) : String {
        var sum = 0.0
        list.forEach { sum += it.amountCount*it.priceDigit }
        return "Gesamtpreis    %.2f€".format(sum)
    }


}
