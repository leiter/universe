package com.together.chat.anyidea

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.FirebaseDatabase
import com.together.R
import com.together.app.MainViewModel
import com.together.order.main.ProductAdapter
import kotlinx.android.synthetic.main.any_idea_fragment.*



class AnyIdeaFragment : Fragment() {

    private val datbase = FirebaseDatabase.getInstance()
    private val dataRef = datbase.reference

    private val presenter = AnyIdeaPresenter(dataRef)

    private lateinit var adapter: ProductAdapter

    private fun getModel(): MainViewModel {
        return ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.any_idea_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        input_message.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    send_message.isEnabled = s.toString().trim().length > 0
            }

        })

        send_message.setOnClickListener {
            presenter.postAnyMessage(input_message.text.toString())
            input_message.setText("")
        }
        send_message.isEnabled = send_message.text.isNotEmpty()


    }



}



