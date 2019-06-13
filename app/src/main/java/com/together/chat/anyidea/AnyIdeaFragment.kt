package com.together.chat.anyidea

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.together.R
import com.together.app.MainViewModel
import com.together.app.UiState
import com.together.repository.storage.getObservable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.any_idea_fragment.*


class AnyIdeaFragment : Fragment() {

    private val dataRef = FirebaseDatabase.getInstance().reference

    private val presenter = AnyIdeaPresenter(dataRef)

    private val adapter: AnyIdeaAdapter = AnyIdeaAdapter()

    private val disposable = CompositeDisposable()

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
                send_message.isEnabled = s.toString().trim().isNotEmpty()
            }

        })

        send_message.setOnClickListener {
            presenter.postAnyMessage(input_message.text.toString())
            input_message.setText("")
        }

        send_message.isEnabled = send_message.text.isNotEmpty()

        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        idea_messages.layoutManager = layoutManager
        idea_messages.adapter = adapter

        val ref = FirebaseDatabase.getInstance().reference
        val anymessage = ref.child("anymessage").getObservable<UiState.ChatMessage>()
        disposable.add(anymessage.subscribe {
            adapter.addMessage(it)
        })


//

    }


}

