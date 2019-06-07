package com.together.order.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.together.R
import com.together.app.MainMessagePipe
import com.together.app.MainViewModel
import com.together.app.UiEvent
import com.together.app.UiState
import com.together.repository.Result
import io.reactivex.Observable
import kotlinx.android.synthetic.main.main_order_fragment.*

class ProductsFragment : Fragment(), ProductAdapter.ItemClicked {

    private lateinit var model: MainViewModel
    private lateinit var adapter: ProductAdapter

    override fun clicked(item: UiState.Article) {
        product_name.text = item.productName
        product_description.text = item.productDescription
        val p = Picasso.Builder(context)
            .downloader(OkHttp3Downloader(context)).build()
        p.load(item.imageUrl).placeholder(R.drawable.ic_shopping_cart_black_24dp).into(product_image)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_order_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        article_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val d = mutableListOf<UiState.Article>()

        adapter = ProductAdapter(d,this)
        article_list.adapter = adapter



        val ref = FirebaseDatabase.getInstance().reference
        val products = ref.child("articles")
        val listener = products.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
             val i = p0.getValue(Result.Article::class.java)!!
                val e = UiState.Article(productName = i.productName,
                    productDescription = i.productDescription, imageUrl = i.imageUrl)
                adapter.addItem(e)

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

//        MainMessagePipe.mainThreadMessage.subscribe {
//            when (it) {
//                is Result.Article -> {
//                    val s = (it).productName
//                    Toast.makeText(
//                        baseContext, "msg Received ${s}.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//            }
//        }


    }

    override fun onResume() {
        super.onResume()
        MainMessagePipe.uiEvent.onNext(UiEvent.LoadProducts)
Observable.create<Result.Article> {



}
    }

}
