package com.together.order

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.together.R
import com.together.create.app.MainMessagePipe
import com.together.create.app.MainViewModel
import com.together.create.app.UiEvent
import com.together.create.app.UiState
import com.together.repository.Result
import com.together.repository.storage.getObservable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fake_toolbar.*
import kotlinx.android.synthetic.main.main_order_fragment.*

class ProductsFragment : Fragment(), ProductAdapter.ItemClicked {

    private lateinit var model: MainViewModel

    private lateinit var adapter: ProductAdapter

    private val disposable = CompositeDisposable()

    override fun clicked(item: UiState.Article) {
        model.presentedProduct.value = item
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_order_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        model.presentedProduct.observe(this, Observer {
            product_name.text = it.productName
            product_description.text = it.productDescription
            val price = it.pricePerUnit + " €/" + it.unit
            product_price.text = price

            val p = Picasso.Builder(context)
                .downloader(OkHttp3Downloader(context)).build()
            p.load(it.remoteImageUrl)
                .into(product_image)
        })

        article_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        adapter = ProductAdapter( this)
        article_list.adapter = adapter

        val ref = FirebaseDatabase.getInstance().reference
        val products = ref.child("articles")
        disposable.add(products.getObservable<Result.Article>().subscribe {
            val e = UiState.Article(
                productName = it.productName,
                productDescription = it.productDescription,
                remoteImageUrl = it.imageUrl,
                pricePerUnit = it.pricePerUnit,
                unit = it.unit

            )
            adapter.addItem(e)
        })

        toolbar_start.setOnClickListener {
            MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }
}
