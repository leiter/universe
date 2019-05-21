package com.together.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.together.R
import com.together.chat.AnyIdeaFragment
import com.together.chat.ChatFragment
import com.together.order.main.OrderFragmentMain
import com.together.repository.storage.Firebase
import com.together.utils.AQ
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*

const val LOGIN_REQUEST = 12

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private val fire = Firebase()


    private var firebaseDatabase: FirebaseDatabase? = null
    private  var articleSource: DatabaseReference? = null

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
                is LoggedState.IN -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ChatFragment()).commit()
                else -> startActivityForResult(AQ.getFirebaseUIStarter(), LOGIN_REQUEST)
            }

        })
        viewModel.loggedState.value = Firebase().isLoggedIn()

        navigation.setOnNavigationItemSelectedListener(selectedListener)


//        firebaseDatabase?.setLogLevel(Logger.Level.DEBUG)
        firebaseDatabase = FirebaseDatabase.getInstance()


    }

    override fun onResume() {
        super.onResume()

        articleSource = firebaseDatabase?.reference?.child("articles")

        articleSource?.addValueEventListener(valueEventListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.loggedState.value = LoggedState.IN()
            } else {
                viewModel.loggedState.value = LoggedState.OUT()
            }
        }
    }


    val childEventListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e("TTTTT", "For debugging");
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            Log.e("TTTTT", "For debugging");
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            Log.e("TTTTT", "For debugging"); }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            Log.e("TTTTT", "For debugging")
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            Log.e("TTTTT", "For debugging"); }

    }

    val valueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e("TTTTT", "For debugging");
        }

        override fun onDataChange(p0: DataSnapshot) {
            Log.e("TTTTT", "For debugging")
        }


    }
}
