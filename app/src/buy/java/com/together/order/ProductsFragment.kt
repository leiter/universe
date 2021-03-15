package com.together.order

import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.base.*
import com.together.databinding.MainOrderFragmentBinding
import com.together.dialogs.InfoDialogFragment
import com.together.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ProductsFragment : BaseFragment(R.layout.main_order_fragment), ProductAdapter.ItemClicked,

    View.OnFocusChangeListener {

    private val adapter: ProductAdapter = ProductAdapter(this)
    private val digitsWithComma = DigitsKeyListener.getInstance("0123456789,")
    private val digitsWithOutComma = DigitsKeyListener.getInstance("0123456789")
    private val viewBinding: MainOrderFragmentBinding by viewLifecycleLazy {
        MainOrderFragmentBinding.bind(requireView())
    }
    private lateinit var amountDisposable: Disposable

    override fun clicked(item: UiState.Article) {
        viewModel.presentedProduct.value = item
        val productData =
            viewModel.productList.value!!.toMutableList()
        val selectedItemIndex = productData.indexOfFirst { it.id == item.id }

        val indexToRemove = productData.filter { !it.isSelected }
        indexToRemove.forEachIndexed { index, _ ->
            val n = productData[index].copy(isSelected = false)
            productData[index] = n
        }

        val i = productData[selectedItemIndex].copy(isSelected = true)
        productData[selectedItemIndex] = i
        adapter.submitList(productData.toMutableList())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            ManageDialog().show(childFragmentManager, ManageDialog.TAG)
        }

        viewBinding.articleList.layoutManager = LinearLayoutManager(context)
        viewBinding.articleList.adapter = adapter
        viewBinding.blocking.visibility = View.VISIBLE

        viewBinding.tvProductName.setOnClickListener {
            if (viewModel.isTablet.not()) {
                val text = viewModel.presentedProduct.value!!.detailInfo
                if (text.isNotEmpty())
                    InfoDialogFragment.newInstance(
                        InfoDialogFragment.SHOW_INFO, text
                    ).show(childFragmentManager, InfoDialogFragment.TAG)
                else requireContext().showLongToast("Es sind keine Infos verfügbar.")
            }
        }

        viewModel.imageLoadingProgress.observe(viewLifecycleOwner,
            { viewBinding.prLoadImageProgress.visibility = View.GONE }
        )

        viewModel.productList.observe(viewLifecycleOwner, {
            if (it.size > 2) viewBinding.blocking.visibility = View.GONE
            adapter.submitList(it.toMutableList())
        })

        viewModel.basket.observe(viewLifecycleOwner, {
            if (it.size == 0) {
                viewBinding.etProductAmount.setText(getString(R.string.zero_count_amount))
                viewBinding.btnActivateCounter.visibility = View.VISIBLE
                viewBinding.counter.counterContainer.visibility = View.INVISIBLE
            }
            viewBinding.btnShowBasket.badgeCount.text = it.size.toString()
        })

        viewBinding.btnShowBasket.badge.setOnClickListener {
            if (viewModel.basket.value!!.size > 0)
                BasketFragment().show(childFragmentManager, BasketFragment.TAG)
            else
                MainMessagePipe.uiEvent.onNext(
                    UiEvent.ShowToast(
                        requireContext(),
                        R.string.empty_basket_msg, Gravity.CENTER
                    )
                )
        }

        viewBinding.etMenuSearchProducts.onFocusChangeListener = this

        viewBinding.etProductAmount.onFocusChangeListener = this

        viewBinding.etProductAmount.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                putIntoBasket()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewBinding.etMenuSearchProducts.textChanges().skipInitialValue()
            .debounce(400, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .map { searchTerm ->
                val productData = viewModel.productList.value!!.toMutableList()
                if (searchTerm.isNotEmpty()) {
                    productData.forEach { it.isSelected = false }
                }

                productData.filter {
                    it.productName.startsWith(searchTerm, ignoreCase = true) ||
                            it.searchTerms.matches(
                                ".*$searchTerm.*".toRegex(RegexOption.IGNORE_CASE)
                            )
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { filtered -> adapter.submitList(filtered.toMutableList()) }
            .addTo(disposable)



        MainMessagePipe.uiEvent.subscribe {
            when (it) {
                is UiEvent.BasketMinusOne -> {
                    viewBinding.btnShowBasket.badgeCount.text =
                        viewModel.basket.value?.size.toString()
                }
            }
        }.addTo(disposable)

        viewModel.presentedProduct.observe(viewLifecycleOwner, {
            viewBinding.tvMenuTitle.text = getString(R.string.bodenschatz_caps)
            viewBinding.btnShowBasket.badge.visibility = View.VISIBLE
            setPresentedProduct(it)
            setupAmountListener()
        })
    }

    private fun inFocus(): UiState.Article {
        return viewModel.presentedProduct.value!!
    }

    private fun setupAmountListener() {
        if (::amountDisposable.isInitialized && amountDisposable.isDisposed.not()) {
            amountDisposable.dispose()
        }
        amountDisposable =
            viewBinding.etProductAmount.textChanges().skipInitialValue()
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        if (it.contains(",") || inFocus().unit.toLowerCase(Locale.ROOT) != "kg") {
                            viewBinding.etProductAmount.keyListener = digitsWithOutComma
                        } else {
                            viewBinding.etProductAmount.keyListener = digitsWithComma
                        }
                        val v = NumberFormat.getInstance().parse(it.toString())!!
                        inFocus().amountCount = v.toDouble()
                        viewBinding.tvPriceAmount.setText(inFocus().priceDisplay)
                    } else {
                        viewBinding.tvPriceAmount.setText(getString(R.string.zero_price_euro))
                    }
                }, { it.printStackTrace() })
    }

    private fun clickPlusOrMinus(isPlus: Boolean) {
        val toAdd = if (isPlus) 1 else -1
        val newVal = inFocus().pieceCounter + toAdd
        if (newVal <= 0) {
            viewBinding.btnActivateCounter.visibility = View.VISIBLE
            viewBinding.counter.counterContainer.visibility = View.INVISIBLE
            val zeroStr =
                if (inFocus().unit == "kg") R.string.zero_kg else R.string.zero_count_amount
            viewBinding.etProductAmount.setText(zeroStr)
            inFocus().pieceCounter = 0
            return
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
        with(viewBinding) {
            btnActivateCounter.visibility = View.VISIBLE
            btnActivateCounter.visibility = View.INVISIBLE
            tvProductCategory.text = product.category
            tvProductName.text = product.productName
            prLoadImageProgress.visibility = View.VISIBLE
            tvEstimatedWeight.text = product.getWeightText()
            val amountCountText = product.calculateAmountCountDisplay()
            etProductAmount.setText(amountCountText)
            etProductAmount.setSelection(amountCountText.length)
            products.clearFocus()
            etProductAmount.requestFocus()

            if (product.unit.toLowerCase(Locale.ROOT) == "kg" && product.weightPerPiece == 0.0) {
                tvEstimatedWeight.hide()
                btnActivateCounter.hide()
            } else {
                tvEstimatedWeight.show()
                btnActivateCounter.show()
            }

            if (viewModel.isTablet) {
                tvProductDetailText.text = product.detailInfo
                tvProductDetailText.movementMethod = ScrollingMovementMethod()
            }

            requireContext().loadImage(ivProductImage, product.remoteImageUrl)
        }


    }

    override fun onFocusChange(p0: View?, p1: Boolean) {
        if (p0?.id == R.id.et_product_amount) {
            if (p1) {
                with(viewBinding) {
                    btnActivateCounter.visibility = View.VISIBLE
                    counter.counterContainer.visibility = View.INVISIBLE
                    btnProductAmountClear.setImageResource(R.drawable.ic_clear)
                    btnProductAmountClear.setOnClickListener {
                        etProductAmount.setText("")
                        btnActivateCounter.visibility = View.VISIBLE
                        counter.counterContainer.visibility = View.INVISIBLE
                        inFocus().pieceCounter = 0
                        inFocus().amountCount = 0.0
                    }
                }
            } else {
                viewBinding.btnProductAmountClear.setImageBitmap(null)
                viewBinding.btnProductAmountClear.setOnClickListener(null)
            }
        }

        if (p0?.id == R.id.et_menu_search_products) {
            if (p1) {
                with(viewBinding) {
                    btnMenuSearch.setImageResource(R.drawable.search_icon)
                    btnMenuSearch.setOnClickListener { etMenuSearchProducts.setText("") }
                }


            } else {
                with(viewBinding) {
                    etMenuSearchProducts.clearFocus()
                    etMenuSearchProducts.hide()
                    etMenuSearchProducts.setText("")
                    tvMenuTitle.show()
                    btnMenuSearch.setImageResource(R.drawable.ic_search_48)
                    btnMenuSearch.setOnClickListener {
                        tvMenuTitle.hide()
                        etMenuSearchProducts.show()
                        etMenuSearchProducts.requestFocus()
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
            if (basketItem.id == p.id) {
                inBasket = index
            }
        }
        if (inBasket != -1) {
            basket.removeAt(inBasket)
            basket.add(inBasket, p)

        } else {
            basket.add(p)
        }
        viewBinding.btnShowBasket.badge.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.shake_rotate)
        )
        if (basket.size == 0) {
            viewBinding.btnShowBasket.badgeCount.hide()
        } else {
            viewBinding.btnShowBasket.badgeCount.show()
            viewBinding.btnShowBasket.badgeCount.text = basket.size.toString()
        }
    }

}
