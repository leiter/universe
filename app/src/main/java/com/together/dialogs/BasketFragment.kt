package com.together.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import kotlinx.android.synthetic.main.fragment_basket.*

class BasketFragment : DialogFragment() {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: BasketAdapter

    lateinit var viewModel: MainViewModel

    private val click : (UiState.Article) -> Unit
        inline get() = { input ->
            val pos = adapter.data.indexOf(adapter.data.first { input.id == it.id })
            viewModel.basket.value?.remove(viewModel.basket.value!!.first { it.id == input.id })
            adapter.notifyItemRemoved(pos)
            MainMessagePipe.uiEvent.onNext(UiEvent.BasketMinusOne)
        }

    override fun onStart() {
        super.onStart()
        if (dialog!=null){
            val w = ViewGroup.LayoutParams.MATCH_PARENT
            val h = ViewGroup.LayoutParams.MATCH_PARENT
//            dialog.window?.setLayout(w,h)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        return inflater.inflate(R.layout.fragment_basket, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = viewModel.basket.value!!
        adapter = BasketAdapter(b,click)
        order_basket.adapter = adapter
        order_basket.layoutManager = LinearLayoutManager(context!!,RecyclerView.VERTICAL,false)

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
