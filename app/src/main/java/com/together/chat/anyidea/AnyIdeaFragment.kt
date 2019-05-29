package com.together.chat.anyidea

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.together.R
import com.together.app.MainViewModel
import com.together.order.main.ArticleAdapter
import com.together.repository.Result
import com.together.repository.auth.FirebaseAuth
import com.together.repository.storage.FireDatabase
import kotlinx.android.synthetic.main.any_idea_fragment.*


class AnyIdeaPresenter(val dataRef: DatabaseReference) {

    fun postAnyMessage(msg: String) {
        if (FirebaseAuth.fireUser != null) {
            val user = FirebaseAuth.fireUser
            val message = Result.ChatMessage(
                user.uid,
                user.displayName ?: " ",
                msg, user.photoUrl?.toString() ?: ""
            )
            fire().createDocument(dataRef, "anymessage", message)
        }
    }



    private fun fire(): FireDatabase = FireDatabase()
}


class AnyIdeaFragment : Fragment() {

    private val datbase = FirebaseDatabase.getInstance()
    private val dataRef = datbase.reference

    private val presenter = AnyIdeaPresenter(dataRef)

    private lateinit var adapter: ArticleAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)




    }

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
                if (s.toString().trim().length > 0) {
                    send_message.isEnabled = true
                } else send_message.isEnabled = false
            }

        })

        send_message.setOnClickListener {
            presenter.postAnyMessage(input_message.text.toString())
            input_message.setText("")
        }
    }



}



