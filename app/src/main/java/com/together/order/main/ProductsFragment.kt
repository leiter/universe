package com.together.order.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.together.R
import com.together.app.MainViewModel
import com.together.app.UiState
import com.together.repository.Result
import com.together.repository.storage.getObservable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.main_order_fragment.*

class ProductsFragment : Fragment(), ProductAdapter.ItemClicked {

    private lateinit var model: MainViewModel
    private lateinit var adapter: ProductAdapter
    private val disposable = CompositeDisposable()

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
        disposable.add(products.getObservable<Result.Article>().subscribe {
            val e = UiState.Article(productName = it.productName,
                    productDescription = it.productDescription, imageUrl = it.imageUrl)
            adapter.addItem(e)
        })
    }

}
