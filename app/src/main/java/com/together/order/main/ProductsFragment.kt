package com.together.order.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.together.R
import com.together.app.MainMessagePipe
import com.together.app.MainViewModel
import com.together.app.UiEvent
import com.together.app.UiState
import kotlinx.android.synthetic.main.main_order_fragment.*

class ProductsFragment : Fragment(), ProductAdapter.ItemClicked {


    private lateinit var model: MainViewModel
    private lateinit var adapter: ProductAdapter

    override fun clicked(item: UiState.Article) {
        product_name.text = item.productName
        product_description.text = item.productDescription
        val p = Picasso.Builder(context)
            .downloader(OkHttp3Downloader(context)).build()
        p.load(item.imageUrl).placeholder(R.drawable.ic_avatar_placeholder_24dp).into(product_image)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_order_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        article_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val d = mutableListOf<UiState.Article>()  //TestData.uiArticleList.toMutableList()

        model = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)

        model.productList.observe(this, Observer {
            adapter.update(it)
        })

        adapter = ProductAdapter(d,this)
        article_list.adapter = adapter


        MainMessagePipe.uiEvent.onNext(UiEvent.LoadProducts)

//
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


}
