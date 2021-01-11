package com.together.order

import android.os.Bundle
import android.os.Handler
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.widget.textChanges
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
import kotlinx.android.synthetic.main.item_plus_minus.*
import kotlinx.android.synthetic.main.main_order_fragment.*
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

class ProductsFragment : BaseFragment(), ProductAdapter.ItemClicked {

    private lateinit var adapter: ProductAdapter
    private lateinit var productData: List<UiState.Article>
    private val focusChangeHandler = FocusListener()
    private val digitsWithComma  = DigitsKeyListener.getInstance("0123456789,")
    private val digitsWithOutComma  = DigitsKeyListener.getInstance("0123456789")
    private var itemIndexScrollTo: Int  = -1

    override fun clicked(item: UiState.Article) { viewModel.presentedProduct.value = item
        if (!::productData.isInitialized){
            productData = adapter.data.toMutableList()
        }
        itemIndexScrollTo = productData.lastIndexOf(item)
        Handler().postDelayed({
            ( article_list.layoutManager!! as LinearLayoutManager)
                .scrollToPositionWithOffset(itemIndexScrollTo,0)
        }, 1000L)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_order_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_menu_search.setOnClickListener {
            tv_menu_title.visibility = View.INVISIBLE
            et_menu_search_products.visibility = View.VISIBLE
            et_menu_search_products.requestFocus()
        }

        viewModel.imageLoadingProgress.observe(viewLifecycleOwner,{ pr_load_image_progress.visibility = View.GONE })

        viewModel.presentedProduct.observe(viewLifecycleOwner, { setPresentedProduct(it) })

        article_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = ProductAdapter(this)
        article_list.adapter = adapter

        fab_add_product.setOnClickListener { putIntoBasket(viewModel.presentedProduct.value!!) }

        val products = Database.providerArticles("FmfwB1HVmMdrVib6dqSXkWauOuP2")  //todo

        disposable.add(products.getObservable<Result.Article>()
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                val e = it.dataArticleToUi()
                adapter.addItem(e)
                if (adapter.data.size == 1) {
                    viewModel.presentedProduct.value = adapter.data[0]
                }
            })

        btn_menu_manage.setOnClickListener {
            ManageDialog().show(requireActivity().supportFragmentManager,"ManageDialog")
        }
        btn_menu_shopping_cart.setOnClickListener {
            if (viewModel.basket.value!!.size > 0)
                BasketFragment().show(requireActivity().supportFragmentManager, "Basket")
            else MainMessagePipe.uiEvent.onNext(
                UiEvent.ShowToast(requireContext(),
                    R.string.empty_basket_msg, Gravity.CENTER
                )
            )
        }

        et_menu_search_products.onFocusChangeListener = focusChangeHandler
        et_product_amount.onFocusChangeListener = focusChangeHandler

        btn_activate_counter.setOnClickListener { clickActivateCounter() }
        btn_plus.setOnClickListener { clickPlus() }
        btn_minus.setOnClickListener { clickMinus() }

        Handler().postDelayed({ // fixme use complete loading data
            disposable.add(
                et_menu_search_products.textChanges()
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .map { searchTerm ->
                        if (!::productData.isInitialized){
                            productData = adapter.data.toMutableList()
                        }
                        productData.filter {
                            it.productName.startsWith(searchTerm,ignoreCase = true) ||
                                    it.searchTerms.matches(".*$searchTerm.*"
                                        .toRegex(RegexOption.IGNORE_CASE))
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { filtered -> adapter.setFilteredList(filtered.toMutableList()) }
            )
        },1000L)

        disposable.add(
            et_product_amount.textChanges()
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        if(it.contains(",")||viewModel.presentedProduct.value!!.unit!="kg" ){
                            et_product_amount.keyListener = digitsWithOutComma
                        } else {
                            et_product_amount.keyListener = digitsWithComma
                        }
                        val v = NumberFormat.getInstance().parse(it.toString())!!
                        viewModel.presentedProduct.value!!.amountCount = v.toDouble()
                        tv_price_amount.setText(viewModel.presentedProduct.value!!.priceDisplay)
                    } else {
                        tv_price_amount.setText("0,00€")
                    }
                }, { it.printStackTrace() })
        )

        disposable.add(MainMessagePipe.uiEvent.subscribe {
            when (it) {
                is UiEvent.BasketMinusOne -> {
                    badge_count.text = viewModel.basket.value?.size.toString()
                }
            }
        })
    }

    private fun clickMinus() {
        val newVal = Integer.parseInt(tv_counter.text.toString()) - 1
        if(newVal==0){
            btn_activate_counter.visibility = View.VISIBLE
            counter.visibility = View.INVISIBLE
            tv_counter.text = "0"
            return
        }
        tv_counter.text = newVal.toString()
    }

    private fun clickPlus() {
        val newVal = Integer.parseInt(tv_counter.text.toString()) + 1
        tv_counter.text = newVal.toString()
    }

    companion object {
        const val TAG: String = "ProductsFragment"
    }

    private fun clickActivateCounter(){
        btn_activate_counter.visibility = View.INVISIBLE
        counter.visibility = View.VISIBLE
        tv_counter.text = "1"
    }

    private fun setPresentedProduct(product:UiState.Article){
        btn_activate_counter.visibility = View.VISIBLE
        counter.visibility = View.INVISIBLE
        tv_product_category.text = product.category
        tv_product_name.text = product.productName
        pr_load_image_progress.visibility = View.VISIBLE
        val amountCountText = product.calculateAmountCountDisplay()
        et_product_amount.setText(amountCountText)
        et_product_amount.setSelection(amountCountText.length)
        products.clearFocus()
        et_product_amount.requestFocus()
        requireContext().loadImage(iv_product_image,product.remoteImageUrl)
    }

    inner class FocusListener : View.OnFocusChangeListener {
        override fun onFocusChange(p0: View?, p1: Boolean) {
            if(p0?.id==R.id.et_product_amount){
                if(p1){
                    btn_product_amount_clear.setImageResource(R.drawable.ic_clear)
                    btn_product_amount_clear.setOnClickListener{ et_product_amount.setText("") }
                } else {
                    btn_product_amount_clear.setImageBitmap(null)
                    btn_product_amount_clear.setOnClickListener(null)
                }
            }

            if(p0?.id == R.id.et_menu_search_products){
                if(p1){
                    btn_menu_search.setImageDrawable(ResourcesCompat.getDrawable(
                        resources,R.drawable.search_icon, requireActivity().theme ))
                    btn_menu_search.setOnClickListener{ et_menu_search_products.setText("") }

                } else {
                    et_menu_search_products.visibility = View.INVISIBLE
                    et_menu_search_products.setText("")
                    tv_menu_title.visibility = View.VISIBLE
                    btn_menu_search.setImageResource(R.drawable.ic_search_48)
                    btn_menu_search.setOnClickListener {
                        tv_menu_title.visibility = View.INVISIBLE
                        et_menu_search_products.visibility = View.VISIBLE
                        et_menu_search_products.requestFocus()
                    }
                }
            }
        }
    }

    private fun putIntoBasket(product: UiState.Article) {
        val inputText = et_product_amount.text.toString()
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
            amount = et_product_amount.text.toString() + " " + product.unit,
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
