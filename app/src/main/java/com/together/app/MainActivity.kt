package com.together.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.together.R
import com.together.chat.ChatFragment
import com.together.chat.anyidea.AnyIdeaFragment
import com.together.order.main.OrderFragmentMain
import com.together.repository.Result
import com.together.repository.auth.FirebaseAuth
import com.together.utils.AQ
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val LOGIN_REQUEST = 12

    private lateinit var viewModel: MainViewModel
    private val fire = FirebaseAuth

    private var firebaseDatabase: FirebaseDatabase? = null
    private var articleSource: DatabaseReference? = null

    private val selectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, OrderFragmentMain()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ChatFragment()).commit()

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AnyIdeaFragment()).commit()

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.loggedState.observe(this, Observer {
            when (it) {
                is UiState.LOGGEDIN -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, OrderFragmentMain()).commit()
                else -> {
                    //set logged out state
                    startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
                }
            }

        })
        viewModel.loggedState.value = fire.isLoggedIn()

        navigation.setOnNavigationItemSelectedListener(selectedListener)

        firebaseDatabase = FirebaseDatabase.getInstance()


        if (savedInstanceState == null) {

        }



        MainMessagePipe.mainThreadMessage.subscribe {
            when (it) {
                is Result.Article -> {
                    val s = (it).productName
                    Toast.makeText(
                        baseContext, "msg Received ${s}.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.loggedState.value = UiState.LOGGEDIN
            } else {
                viewModel.loggedState.value = UiState.LOGGEDOUT
            }
        } else {
            finish()
        }
    }


}
