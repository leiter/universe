package com.together.order

import android.os.Bundle
import android.os.Handler
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.together.dialogs.ManageDialog
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.storage.getObservable
import com.together.utils.dataArticleToUi
import com.together.utils.loadImage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.batch_it.*
import kotlinx.android.synthetic.main.main_order_fragment.*
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

class ProductsFragment : BaseFragment(), ProductAdapter.ItemClicked {

    private lateinit var adapter: ProductAdapter
    private lateinit var productData: List<UiState.Article>
    private val focusChangeHandler = FocusListener()

    private var mode: Int? = null

    private val digitsWithComma  = DigitsKeyListener.getInstance("0123456789,")
    private val digitsWithOutComma  = DigitsKeyListener.getInstance("0123456789")

    private lateinit var picasso: Picasso

    override fun clicked(item: UiState.Article) {

        val b = viewModel.basket.value?.find { item._id == it._id }
        if (b == null) {
            viewModel.presentedProduct.value = item
        } else {
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
//        picasso = Picasso.Builder(context).downloader(OkHttp3Downloader(context)).build()
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        viewModel.imageLoadingProgress.observe(viewLifecycleOwner,{
            load_image_progress.visibility = View.GONE
        })

        viewModel.presentedProduct.observe(viewLifecycleOwner, {
            title.text = it.productName
            sub_title.text = it.productDescription
            load_image_progress.visibility = View.VISIBLE
            val amoutCountText = it.calculateAmountCountDisplay()
            product_amount.setText(amoutCountText)
            product_amount.setSelection(amoutCountText.length)
            products.clearFocus()
            product_amount.requestFocus()
            requireContext().loadImage(product_image,it.remoteImageUrl)
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

        btn_manage.setOnClickListener {
            ManageDialog().show(requireActivity().supportFragmentManager,"ManageDialog")
        }
        btn_delete_product.setOnClickListener {
            if (viewModel.basket.value!!.size > 0)
                BasketFragment().show(requireActivity().supportFragmentManager, "Basket")
            else MainMessagePipe.uiEvent.onNext(
                UiEvent.ShowToast(
                    requireContext(),
                    R.string.empty_basket_msg, Gravity.CENTER
                )
            )
        }

        product_search.onFocusChangeListener = focusChangeHandler
        product_amount.onFocusChangeListener = focusChangeHandler

        Handler().postDelayed({ // fixme use complete loading data
            disposable.add(
                product_search.textChanges()
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .map { searchTerm ->
                        if (!::productData.isInitialized){
                            productData = adapter.data.toMutableList()
                        }
                        productData.filter {
                            it.productName.startsWith(searchTerm,ignoreCase = true)
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { filtered -> adapter.setFilteredList(filtered.toMutableList()) }
            )
        },1000L)


        disposable.add(
            product_amount.textChanges()
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        if(it.contains(",")||viewModel.presentedProduct.value!!.unit!="kg" ){
                            product_amount.keyListener = digitsWithOutComma
                        } else {
                            product_amount.keyListener = digitsWithComma
                        }
                        val v = NumberFormat.getInstance().parse(it.toString())!!
                        var i = v.toDouble() *
                                NumberFormat.getInstance()
                                    .parse(viewModel.presentedProduct.value!!.pricePerUnit)!!
                                    .toDouble()
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

    companion object {
        const val TAG: String = "ProductsFragment"
        const val MODE_PARAM = "mode"
    }

    inner class FocusListener : View.OnFocusChangeListener {
        override fun onFocusChange(p0: View?, p1: Boolean) {
            if(p0?.id==R.id.product_search){
                if(p1){
                    btn_product_search.setImageResource(R.drawable.ic_clear)
                    btn_product_search.isClickable = true
                    btn_product_search.setOnClickListener{ product_search.setText("") }
                } else {
                    btn_product_search.setImageResource(R.drawable.ic_search)
                    btn_product_search.setOnClickListener(null)
                    btn_product_search.isClickable = false
                }
            }

            if(p0?.id==R.id.product_amount){
                if(p1){
                    btn_product_amount_clear.setImageResource(R.drawable.ic_clear)
                    btn_product_amount_clear.setOnClickListener{ product_amount.setText("") }
                } else {
                    btn_product_amount_clear.setImageBitmap(null)
                    btn_product_amount_clear.setOnClickListener(null)
                }
            }
        }
    }

    private fun putIntoBasket(product: UiState.Article) {
        val inputText = product_amount.text.toString()
        if(inputText.isEmpty()|| inputText.isBlank()){
            MainMessagePipe.uiEvent.onNext(
                UiEvent.ShowToast(requireContext(), R.string.product_amount_empty, Gravity.CENTER)
            )
            return
        }
        val amountCount = inputText.replace(",", ".").toDouble()
        if (amountCount == 0.0) {
            MainMessagePipe.uiEvent.onNext(
                UiEvent.ShowToast(requireContext(),
                    R.string.product_amount_is_null, Gravity.CENTER)
            )
            return
        }
        product.amountCount = amountCount
        val p = product.copy(
            amount = product_amount.text.toString() + " " + product.unit,
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
