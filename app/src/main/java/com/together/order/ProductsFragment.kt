package com.together.order

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.picasso.OkHttp3Downloader
import com.jakewharton.rxbinding3.widget.textChanges
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.repository.Result
import com.together.repository.storage.getObservable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fake_toolbar.*
import kotlinx.android.synthetic.main.main_order_fragment.*
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

class ProductsFragment : Fragment(), ProductAdapter.ItemClicked, ProductView {

    private lateinit var model: MainViewModel

    private lateinit var adapter: ProductAdapter

    private val disposable = CompositeDisposable()

    private val presenter = ProductsPresenter(this)

    override fun clicked(item: UiState.Article) {
        model.presentedProduct.value = item
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_order_fragment, container, false)
    }

    override fun giveFragmentManager(): FragmentManager {
        return fragmentManager!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        model = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        model.presentedProduct.observe(this, Observer {
            title.text = it.productName
            sub_title.text = it.productDescription
            load_image_progress.visibility = View.VISIBLE
            price_amount.setText("0,00€")
            product_amount.setText("0")
            products.clearFocus()
            setupSpinner(it)

            val p = Picasso.Builder(context)
                .downloader(OkHttp3Downloader(context)).build()
            p.load(it.remoteImageUrl).into(product_image, object : Callback {
                override fun onSuccess() {
                    load_image_progress.visibility = View.GONE
                }

                override fun onError() {
                    load_image_progress.visibility = View.GONE
                }
            })
        })

        article_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        adapter = ProductAdapter(this)
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
            if (adapter.data.size == 1) {
                model.presentedProduct.value = adapter.data[0]
            }
        })

        toolbar_start.setOnClickListener { MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START)) }

        toolbar_end_2.visibility = View.VISIBLE
        toolbar_end_2.setImageResource(R.drawable.ic_shopping_basket)

//        toolbar_end_1.visibility = View.VISIBLE
//        toolbar_end_1.setImageResource(R.drawable.ic_shopping_basket)

        edit_products.setOnClickListener { presenter.startEditProducts() }


        disposable.add(product_amount.textChanges()
            .debounce(400, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.length > 0 ) {

                    val v = NumberFormat.getInstance().parse(it.toString())
                    var i = v.toDouble() *
                            NumberFormat.getInstance().parse(model.presentedProduct.value!!.pricePerUnit).toDouble()
                    i = Math.round(i*100.0) /100.0
                    val s = "%.2f€".format(i)//i.toString()

                    price_amount.setText(s)
                } else {
                    price_amount.setText("0")
                }
            },{
                it.printStackTrace()
            }


            ))


    }

    private fun setupSpinner(article: UiState.Article) {
        val unit = article.unit.split(",")

        val spinnerAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, unit)
        unit_picker.adapter = spinnerAdapter
        unit_picker.onItemSelectedListener = UnitSelectedListener()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }
}


class UnitSelectedListener : AdapterView.OnItemSelectedListener {

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {


    }

}