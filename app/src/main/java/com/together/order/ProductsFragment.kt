package com.together.order

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.picasso.OkHttp3Downloader
import com.jakewharton.rxbinding3.widget.textChanges
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.together.R
import com.together.base.*
import com.together.dialogs.BasketFragment
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.storage.getObservable
import com.together.utils.dataArticleToUi
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.batch_it.*
import kotlinx.android.synthetic.main.main_order_fragment.*
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

class ProductsFragment : BaseFragment(), ProductAdapter.ItemClicked {

    private lateinit var adapter: ProductAdapter

    private var mode: Int? = null

    private lateinit var picasso: Picasso


    override fun clicked(item: UiState.Article) {

        val b = viewModel.basket.value?.find { item._id == it._id }
        if (b == null) {
            viewModel.presentedProduct.value = item
        } else {
//            item.amountCount = b.amountCount
//            item.amount = b.amount
//            item.price = b.price
            viewModel.presentedProduct.value = item
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        price_amount.setText("0,00€")
        picasso = Picasso.Builder(context).downloader(OkHttp3Downloader(context)).build()
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        viewModel.presentedProduct.observe(viewLifecycleOwner, Observer {
            title.text = it.productName
            sub_title.text = it.productDescription
            load_image_progress.visibility = View.VISIBLE
            val amoutCountText = it.amountCountDisplay
            product_amount.setText(it.calculateAmountCountDisplay())
            product_amount.setSelection(amoutCountText.length)
            products.clearFocus()
            product_amount.requestFocus()
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
            putIntoBasket(viewModel.presentedProduct.value!!)
        }

        val products = Database.providerArticles("FmfwB1HVmMdrVib6dqSXkWauOuP2")  //todo

        disposable.add(products.getObservable<Result.Article>()
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
            val e = it.dataArticleToUi()
            adapter.addItem(e)
            if (adapter.data.size == 1) {
                viewModel.presentedProduct.value = adapter.data[0]
            }
        })

        toolbar_start.setOnClickListener { MainMessagePipe.uiEvent.onNext(UiEvent.OpenDrawer) }
        toolbar_end_2.setOnClickListener {
            if (viewModel.basket.value!!.size > 0)
                BasketFragment().show(requireActivity().supportFragmentManager, "Basket")
            else MainMessagePipe.uiEvent.onNext(UiEvent.ShowToast(requireContext(),
                R.string.empty_basket_msg, Gravity.CENTER))
        }


        disposable.add(
            product_amount.textChanges()
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        val v = NumberFormat.getInstance().parse(it.toString())!!
                        var i = v.toDouble() *
                                NumberFormat.getInstance()
                                    .parse(viewModel.presentedProduct.value!!.pricePerUnit)!!.toDouble()
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
                    badge_count.text = viewModel.basket.value?.size.toString()
                }
            }
        })

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
        val amountCount = product_amount.text.toString().replace(",",".").toDouble()
        product.amountCount = amountCount
       val p =  product.copy(
            amount = product_amount.text.toString()+ " " + product.unit,
            amountCount = amountCount
       )

        val basket = viewModel.basket.value!!
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
}
