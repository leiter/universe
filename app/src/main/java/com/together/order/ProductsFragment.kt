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
import com.together.base.BaseFragment
import com.together.base.MainMessagePipe
import com.together.base.UiEvent
import com.together.base.UiState
import com.together.databinding.MainOrderFragmentBinding
import com.together.dialogs.BasketFragment
import com.together.dialogs.ManageDialog
import com.together.utils.loadImage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

class ProductsFragment : BaseFragment(), ProductAdapter.ItemClicked {

    private lateinit var adapter: ProductAdapter
    private lateinit var productData: List<UiState.Article>
    private val focusChangeHandler = FocusListener()
    private val digitsWithComma = DigitsKeyListener.getInstance("0123456789,")
    private val digitsWithOutComma = DigitsKeyListener.getInstance("0123456789")
    private var itemIndexScrollTo: Int = -1
    private lateinit var viewBinding: MainOrderFragmentBinding

    override fun clicked(item: UiState.Article) {
        viewModel.presentedProduct.value = item
        if (!::productData.isInitialized) {
            productData = adapter.data.toMutableList()
        }
        itemIndexScrollTo = productData.lastIndexOf(item)
        Handler().postDelayed({
            (viewBinding.articleList.layoutManager!! as LinearLayoutManager)
                .scrollToPositionWithOffset(itemIndexScrollTo, 0)
        }, 1000L)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = MainOrderFragmentBinding.inflate(
            LayoutInflater.from(requireContext()),container,false)
        viewBinding.btnActivateCounter.setOnClickListener { clickActivateCounter() }
        viewBinding.counter.btnPlus.setOnClickListener { clickPlusOrMinus(true) }
        viewBinding.counter.btnMinus.setOnClickListener { clickPlusOrMinus(false) }

        viewBinding.btnMenuSearch.setOnClickListener {
            viewBinding.tvMenuTitle.visibility = View.INVISIBLE
            viewBinding.etMenuSearchProducts.visibility = View.VISIBLE
            viewBinding.etMenuSearchProducts.requestFocus()
        }

        viewBinding.fabAddProduct.setOnClickListener { putIntoBasket() }

        viewBinding.btnManageProfile.setOnClickListener {
            ManageDialog().show(requireActivity().supportFragmentManager, "ManageDialog")
        }

        adapter = ProductAdapter(this)
        viewBinding.articleList.layoutManager = LinearLayoutManager(context,
            RecyclerView.VERTICAL, false)
        viewBinding.articleList.adapter = adapter

        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.imageLoadingProgress.observe(viewLifecycleOwner,
            { viewBinding.prLoadImageProgress.visibility = View.GONE }

        )

        viewModel.presentedProduct.observe(viewLifecycleOwner, {
            setPresentedProduct(it);
            viewBinding.blocking.visibility = View.GONE
            viewBinding.tvMenuTitle.text = "BODENSCHÄTZE"
            viewBinding.btnShowBasket.badge.visibility = View.VISIBLE
        })

        viewModel.productList.observe(viewLifecycleOwner, {
            adapter.setFilteredList(it.toMutableList())
        })

        viewBinding.btnShowBasket.badge.setOnClickListener {
            if (viewModel.basket.value!!.size > 0)
                BasketFragment().show(requireActivity().supportFragmentManager, "Basket")
            else MainMessagePipe.uiEvent.onNext(
                UiEvent.ShowToast(
                    requireContext(),
                    R.string.empty_basket_msg, Gravity.CENTER
                )
            )
        }

        viewBinding.etMenuSearchProducts.onFocusChangeListener = focusChangeHandler

        viewBinding.etProductAmount.onFocusChangeListener = focusChangeHandler

        disposable.add(
            viewBinding.etMenuSearchProducts.textChanges().skipInitialValue()
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map { searchTerm ->
                    if (!::productData.isInitialized) {
                        productData = adapter.data.toMutableList()
                    }
                    productData.filter {
                        it.productName.startsWith(searchTerm, ignoreCase = true) ||
                                it.searchTerms.matches(
                                    ".*$searchTerm.*".toRegex(RegexOption.IGNORE_CASE))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { filtered -> adapter.setFilteredList(filtered.toMutableList()) }
        )

        disposable.add(
            viewBinding.etProductAmount.textChanges()
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        if (it.contains(",") || inFocus().unit != "kg") {
                            viewBinding.etProductAmount.keyListener = digitsWithOutComma
                        } else {
                            viewBinding.etProductAmount.keyListener = digitsWithComma
                        }
                        val v = NumberFormat.getInstance().parse(it.toString())!!
                        inFocus().amountCount = v.toDouble()
                        viewBinding.tvPriceAmount.setText(inFocus().priceDisplay)
                    } else {
                        viewBinding.tvPriceAmount.setText("0,00€")
                    }
                }, { it.printStackTrace() })
        )

        disposable.add(MainMessagePipe.uiEvent.subscribe {
            when (it) {
                is UiEvent.BasketMinusOne -> {
                    viewBinding.btnShowBasket.badgeCount.text = viewModel.basket.value?.size.toString()
                }
            }
        })
    }

    private fun inFocus(): UiState.Article {
        return viewModel.presentedProduct.value!!
    }

    private fun clickPlusOrMinus(isPlus: Boolean) {
        val toAdd = if (isPlus) 1 else -1
        var newVal = inFocus().pieceCounter + toAdd
        if (newVal <= 0) {
            viewBinding.btnActivateCounter.visibility = View.VISIBLE
            viewBinding.counter.counterContainer.visibility = View.INVISIBLE
            newVal = 0
        }
        inFocus().pieceCounter = newVal
        val v = inFocus().calculateAmountCountDisplay()
        viewBinding.etProductAmount.setText(v)
        viewBinding.counter.tvCounter.text = newVal.toString()
    }

    companion object {
        const val TAG: String = "ProductsFragment"
    }

    private fun clickActivateCounter() {
        viewBinding.btnActivateCounter.visibility = View.INVISIBLE
        viewBinding.counter.counterContainer.visibility = View.VISIBLE
        val amountStr = viewBinding.etProductAmount.text.toString()
        val isEmpty = inFocus().amountCount == 0.0 || amountStr.isEmpty()
        if (isEmpty) {
            inFocus().pieceCounter = 1
            viewBinding.etProductAmount.setText(inFocus().calculateAmountCountDisplay())
            viewBinding.counter.tvCounter.text = "1"
        } else {
            val a = (amountStr.replace(",", ".")
                .toDouble() / inFocus().weightPerPiece).toInt() + 1
            inFocus().pieceCounter = a
            viewBinding.etProductAmount.setText(inFocus().calculateAmountCountDisplay())
            viewBinding.counter.tvCounter.text = a.toString()
        }
    }

    private fun setPresentedProduct(product: UiState.Article) {
        viewBinding.btnActivateCounter.visibility = View.VISIBLE
        viewBinding.btnActivateCounter.visibility = View.INVISIBLE
        viewBinding.tvProductCategory.text = product.category
        viewBinding.tvProductName.text = product.productName
        viewBinding.prLoadImageProgress.visibility = View.VISIBLE
        viewBinding.tvEstimatedWeight.text = product.getWeightText()
        val amountCountText = product.calculateAmountCountDisplay()
        viewBinding.etProductAmount.setText(amountCountText)
        viewBinding.etProductAmount.setSelection(amountCountText.length)
        viewBinding.products.clearFocus()
        viewBinding.etProductAmount.requestFocus()
        requireContext().loadImage(viewBinding.ivProductImage, product.remoteImageUrl)
    }

    inner class FocusListener : View.OnFocusChangeListener {
        override fun onFocusChange(p0: View?, p1: Boolean) {
            if (p0?.id == R.id.et_product_amount) {
                if (p1) {
                    viewBinding.btnActivateCounter.visibility = View.VISIBLE
                    viewBinding.counter.counterContainer.visibility = View.INVISIBLE
                    viewBinding.btnProductAmountClear.setImageResource(R.drawable.ic_clear)
                    viewBinding.btnProductAmountClear.setOnClickListener {
                        viewBinding.etProductAmount.setText("")
                        viewBinding.btnActivateCounter.visibility = View.VISIBLE
                        viewBinding.counter.counterContainer.visibility = View.INVISIBLE
                        inFocus().pieceCounter = 0
                        inFocus().amountCount = 0.0
                    }
                } else {
                    viewBinding.btnProductAmountClear.setImageBitmap(null)
                    viewBinding.btnProductAmountClear.setOnClickListener(null)
                }
            }

            if (p0?.id == R.id.et_menu_search_products) {
                if (p1) {
                    viewBinding.btnMenuSearch.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources, R.drawable.search_icon, requireActivity().theme
                        )
                    )
                    viewBinding.btnMenuSearch.setOnClickListener { viewBinding.etMenuSearchProducts.setText("") }

                } else {
                    viewBinding.etMenuSearchProducts.visibility = View.INVISIBLE
                    viewBinding.etMenuSearchProducts.setText("")
                    viewBinding.tvMenuTitle.visibility = View.VISIBLE
                    viewBinding.btnMenuSearch.setImageResource(R.drawable.ic_search_48)
                    viewBinding.btnMenuSearch.setOnClickListener {
                        viewBinding.tvMenuTitle.visibility = View.INVISIBLE
                        viewBinding.etMenuSearchProducts.visibility = View.VISIBLE
                        viewBinding.etMenuSearchProducts.requestFocus()
                    }
                }
            }
        }
    }

    private fun putIntoBasket() {
        val product = inFocus()
        val inputText = viewBinding.etProductAmount.text.toString()
        if (inputText.isEmpty() || inputText.isBlank()) {
            MainMessagePipe.uiEvent.onNext(
                UiEvent.ShowToast(requireContext(), R.string.product_amount_empty, Gravity.CENTER)
            )
            return
        }
        val amountCount = inputText.replace(",", ".").toDouble()
        if (amountCount == 0.0) {
            MainMessagePipe.uiEvent.onNext(
                UiEvent.ShowToast(
                    requireContext(),
                    R.string.product_amount_is_null, Gravity.CENTER
                )
            )
            return
        }
        product.amountCount = amountCount
        val p = product.copy(
            amount = viewBinding.etProductAmount.text.toString() + " " + product.unit,
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
            viewBinding.btnShowBasket.badgeCount.visibility = View.INVISIBLE
        } else {
            viewBinding.btnShowBasket.badgeCount.visibility = View.VISIBLE
            viewBinding.btnShowBasket.badgeCount.text = basket.size.toString()
        }
    }
}
