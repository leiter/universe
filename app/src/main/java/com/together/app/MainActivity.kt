package com.together.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import com.together.R
import com.together.chat.ChatFragment
import com.together.chat.anyidea.AnyIdeaFragment
import com.together.order.main.OrderFragmentMain
import com.together.repository.Result
import com.together.repository.auth.FirebaseAuth
import com.together.repository.storage.DatabaseManager
import com.together.utils.AQ
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val LOGIN_REQUEST = 12

    private lateinit var viewModel: MainViewModel
    private val fire = FirebaseAuth

    private lateinit var firebaseDatabase: FirebaseDatabase

    companion object {

        private val LOGIN_ACTION = "action.login"

        fun startLogin(context: Context) {
            val i = Intent(context, MainActivity::class.java).apply {
                action = context.packageName + LOGIN_ACTION
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(i)
        }
    }

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
//                    startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
                }
            }

        })
        viewModel.loggedState.value = fire.isLoggedIn()

        navigation.setOnNavigationItemSelectedListener(selectedListener)

        firebaseDatabase = FirebaseDatabase.getInstance()


        button_next.setOnClickListener {

            //startLogin(baseContext)
//            FireData().getSupplyList(firebaseDatabase!!.reference)
            DatabaseManager.ARTICLE_LIST.setup(firebaseDatabase.reference)
        }




    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.action == packageName + LOGIN_ACTION) {
            startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
        }

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
