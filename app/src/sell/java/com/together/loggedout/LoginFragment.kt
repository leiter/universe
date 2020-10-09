package com.together.loggedout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.together.R
import com.together.app.MainActivity
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plus_one_button.setOnClickListener {
            MainActivity.startLogin(requireActivity())
            it.visibility = View.GONE
            val fm = activity?.supportFragmentManager!!
                fm.beginTransaction().remove(this@LoginFragment)
        }
    }
}
