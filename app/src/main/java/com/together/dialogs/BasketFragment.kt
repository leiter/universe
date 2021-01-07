package com.together.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
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

    private var param1: String? = null
    private var param2: String? = null

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
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = requireActivity().layoutInflater.inflate(R.layout.fragment_basket, null)
        val b = viewModel.basket.value!!
        adapter = BasketAdapter(b, clickToDelete)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.order_basket)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        return AlertDialog.Builder(requireActivity(),R.style.MyDialogTheme).setView(layout).create()
    }

    override fun onResume() {
        super.onResume()
//        val lp = WindowManager.LayoutParams()
//        lp.copyFrom(dialog!!.window!!.attributes)
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT
//
//        dialog!!.window!!.attributes = lp
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    companion object {

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BasketFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
