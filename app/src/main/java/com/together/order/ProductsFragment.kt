package com.together.order

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.picasso.OkHttp3Downloader
import com.jakewharton.rxbinding3.widget.textChanges
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.together.R
import com.together.base.MainMessagePipe
import com.together.base.MainViewModel
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.dialogs.BasketFragment
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.storage.getObservable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.batch_it.*
import kotlinx.android.synthetic.main.main_order_fragment.*
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

class ProductsFragment : Fragment(), ProductAdapter.ItemClicked, ProductView {

    private lateinit var model: MainViewModel

    private lateinit var adapter: ProductAdapter

    private val disposable = CompositeDisposable()

    private var mode: Int? = null

    private val presenter = ProductsPresenter(this)

    private lateinit var picasso: Picasso


    override fun clicked(item: UiState.Article) {
        product_amount.setText("")
        val b = model.basket.value?.find { item._id == it._id }
        if (b == null) {
            model.presentedProduct.value = item
        } else {
            item.amount = b.amount
            item.price = b.price
            model.presentedProduct.value = item
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let { mode = it.getInt(MODE_PARAM) }
        return inflater.inflate(R.layout.main_order_fragment, container, false)
    }

    override fun giveFragmentManager(): FragmentManager {
        return requireFragmentManager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        picasso = Picasso.Builder(context).downloader(OkHttp3Downloader(context)).build()

        model = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        model.presentedProduct.observe(viewLifecycleOwner, Observer {
            title.text = it.productName
            sub_title.text = it.productDescription
            load_image_progress.visibility = View.VISIBLE
            price_amount.setText(it.price)
            product_amount.setText(it.amount)
            products.clearFocus()
            setupSpinner(it)

            picasso.load(it.remoteImageUrl)
                .error(R.drawable.obst_1)
                .into(product_image, object : Callback {
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

        add_product.setOnClickListener {
            putIntoBasket(model.presentedProduct.value!!)
        }

        val products = Database.providerArticles("Qx69mYNTkDMS55V2paSztcwEAPN2")  //todo

        disposable.add(products.getObservable<Result.Article>().observeOn(AndroidSchedulers.mainThread()).subscribe {
            val unitList = prepareUnitData(it)
            val defaultUnit = unitList.firstOrNull { it.name == "kg" }
            val indexOfDefault = if (defaultUnit != null) unitList.indexOf(defaultUnit) else 0

            val e = UiState.Article(
                _id = it.id,
                productName = it.productName,
                productDescription = it.productDescription,
                remoteImageUrl = it.imageUrl,

                units = unitList,

                unit = unitList[indexOfDefault].name,
                pricePerUnit = unitList[indexOfDefault].price,
                discount = it.discount,
                _mode = it.mode,
                available = it.available
            )
            adapter.addItem(e)
            if (adapter.data.size == 1) {
                model.presentedProduct.value = adapter.data[0]
            }
        })

        toolbar_start.setOnClickListener { MainMessagePipe.uiEvent.onNext(UiEvent.OpenDrawer) }
        toolbar_end_2.setOnClickListener {
            if (model.basket.value!!.size > 0)
                BasketFragment().show(requireFragmentManager(), "Basket")
            else MainMessagePipe.uiEvent.onNext(UiEvent.ShowToast(requireContext(), R.string.empty_basket_msg, Gravity.CENTER))
        }

        edit_products.setOnClickListener { presenter.startEditProducts() }

        disposable.add(
            product_amount.textChanges()
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        val v = NumberFormat.getInstance().parse(it.toString())
                        var i = v.toDouble() *
                                NumberFormat.getInstance()
                                    .parse(model.presentedProduct.value!!.pricePerUnit).toDouble()
                        i = Math.round(i * 100.0) / 100.0
                        val s = "%.2f€".format(i)
                        price_amount.setText(s)
                    } else {
                        price_amount.setText("0,00€")
                    }
                }, {
                    it.printStackTrace()
                })
        )

        disposable.add(MainMessagePipe.uiEvent.subscribe {
            when (it) {
                is UiEvent.BasketMinusOne -> {
                    badge_count.text = model.basket.value?.size.toString()
                }
            }
        })

    }

    private fun setupSpinner(article: UiState.Article) {
        val spinnerAdapter = UnitSpinnerAdapter(requireContext(), article.units)
        unit_picker.adapter = spinnerAdapter
        unit_picker.onItemSelectedListener = UnitSelectedListener(article.units)
    }

    private fun prepareUnitData(article: Result.Article): List<UiState.Unit> {
        // units = "2,50:€:kg;0,50:€:Bund"
        val units = article.units.split(";")
        val result = mutableListOf<UiState.Unit>()
        units.forEach {
            val p = it.split(":")
            val w: String = if (p.size > 1) p[1] else ""
            val r = UiState.Unit(
                name = p[0],
                price = p[1],
                averageWeight = w
            )
            result.add(r)
        }
        return result.toList()

    }


    private val map = HashMap<String, Int>().apply {
        put("kg", 0)
        put("Stück", 1)
        put("Bund", 2)
        put("Schale", 3)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    companion object {

        const val TAG: String = "ProductsFragment"
        const val MODE_PARAM = "mode"
        const val BUYER = 0
        const val SELLER = 1

        fun newInstance(mode: Int): ProductsFragment {
            return ProductsFragment().apply {
                arguments = Bundle().apply {
                    putInt(MODE_PARAM, mode)
                }
            }
        }
    }

    private fun putIntoBasket(product: UiState.Article) {
        val p = UiState.Article(
            productId = product.productId,
            productName = product.productName,
            productDescription = product.productDescription,
            _id = product._id,
            amount = product_amount.text.toString(),
            price = price_amount.text.toString()
        )
        val basket = model.basket.value!!
        var inBasket = -1
        basket.forEachIndexed { index, basketItem ->
            if (basketItem._id == p._id) {
                inBasket = index
            }
        }
        if (inBasket != -1) {
            basket.removeAt(inBasket)
            basket.add(inBasket, p)

        } else {
            basket.add(p)
        }

        if (basket.size == 0) {
            badge_count.visibility = View.INVISIBLE
        } else {
            badge_count.visibility = View.VISIBLE
            badge_count.text = basket.size.toString()
        }
    }

    inner class UnitSelectedListener(private val unitList: List<UiState.Unit>) : AdapterView.OnItemSelectedListener {

        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            map[unitList[position].name]
        }

    }

}


