package com.together.order

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.base.*
import com.together.basket.BasketFragment
import com.together.databinding.MainOrderFragmentBinding
import com.together.dialogs.InfoDialogFragment
import com.together.manage.ManageDialog
import com.together.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ProductsFragment : BaseFragment(R.layout.main_order_fragment),
    ProductAdapter.ItemClicked,
    View.OnFocusChangeListener {

    private val adapter: ProductAdapter = ProductAdapter(this)

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

        with(viewBinding){
            btnActivateCounter.setOnClickListener { clickActivateCounter() }
            counter.btnPlus.setOnClickListener { clickPlusOrMinus(true) }
            counter.btnMinus.setOnClickListener { clickPlusOrMinus(false) }

            btnMenuSearch.setOnClickListener {
                tvMenuTitle.visibility = View.INVISIBLE
                etMenuSearchProducts.visibility = View.VISIBLE
                etMenuSearchProducts.requestFocus()
            }

            fabAddProduct.setOnClickListener { putIntoBasket() }

            btnManageProfile.setOnClickListener {
                ManageDialog().show(childFragmentManager, ManageDialog.TAG)
            }

            articleList.layoutManager = LinearLayoutManager(context)
            articleList.adapter = adapter
            blocking.show()

            tvProductName.setOnClickListener {
                if (viewModel.isTablet.not()) {
                    val text = viewModel.presentedProduct.value!!.detailInfo
                    if (text.isNotEmpty())
                        InfoDialogFragment.newInstance(
                            InfoDialogFragment.SHOW_INFO, text
                        ).show(childFragmentManager, InfoDialogFragment.TAG)
                    else requireContext().showLongToast("Es sind keine Infos verfügbar.")
                }
            }
            etMenuSearchProducts.onFocusChangeListener = this@ProductsFragment
            etProductAmount.onFocusChangeListener = this@ProductsFragment

            viewModel.imageLoadingProgress.observe(viewLifecycleOwner,
                { prLoadImageProgress.remove() }
            )

            viewModel.productList.observe(viewLifecycleOwner, {
                if (it.size > 2) blocking.remove()
                adapter.submitList(it.toMutableList())
            })

            viewModel.basket.observe(viewLifecycleOwner, {
                if (it.size == 0) {
                    etProductAmount.setText(getString(R.string.zero_count_amount))
                    btnActivateCounter.show()
                    counter.counterContainer.hide()
                }
                btnShowBasket.badgeCount.text = it.size.toString()
            })

            btnShowBasket.badge.setOnClickListener {
                if (viewModel.basket.value!!.size > 0)
                    BasketFragment().show(childFragmentManager, BasketFragment.TAG)
                else
                    viewModel.snacks.value = UiEvent.Snack(
                        msg = R.string.empty_basket_msg)
            }

            etProductAmount.setOnEditorActionListener { _, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) { putIntoBasket()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

            etMenuSearchProducts.textChanges().skipInitialValue()
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
                        btnShowBasket.badgeCount.text =
                            viewModel.basket.value?.size.toString()
                    }
                }
            }.addTo(disposable)

            viewModel.presentedProduct.observe(viewLifecycleOwner, {
                tvMenuTitle.text = getString(R.string.bodenschatz_caps)
                btnShowBasket.badge.show()
                setPresentedProduct(it)
                setupAmountListener()
            })
        }
    }

    private fun inFocus(): UiState.Article { return viewModel.presentedProduct.value!! }

    private fun setupAmountListener() {
        if (::amountDisposable.isInitialized && amountDisposable.isDisposed.not()) {
            amountDisposable.dispose()
        }
        amountDisposable =
            viewBinding.etProductAmount.textChanges()
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
            viewBinding.btnActivateCounter.show()
            viewBinding.counter.counterContainer.hide()
            val zeroStr =
                if (inFocus().unit.toLowerCase(Locale.ROOT) == "kg") R.string.zero_kg else R.string.zero_count_amount
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
        viewBinding.btnActivateCounter.hide()
        viewBinding.counter.counterContainer.show()
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
            btnActivateCounter.show()
            btnActivateCounter.hide()
            tvProductCategory.text = product.category
            tvProductName.text = product.productName
            prLoadImageProgress.show()
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
                        if(!(inFocus().unit.toLowerCase(Locale.ROOT) == "kg"
                            && inFocus().weightPerPiece == 0.0)){
                            btnActivateCounter.show()
                        }
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
                    btnMenuSearch.setOnClickListener {
                        val t = etMenuSearchProducts.text.toString()
                        if(t==""){
                            etMenuSearchProducts.clearFocus()
                            etMenuSearchProducts.hideIme()
                            return@setOnClickListener
                            }
                        etMenuSearchProducts.setText("")
                    }
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
            viewModel.snacks.value = UiEvent.Snack(msg = R.string.product_amount_empty)
            return
        }
        val amountCount = inputText.replace(",", ".").toDouble()
        if (amountCount == 0.0) {
            viewModel.snacks.value = UiEvent.Snack(msg = R.string.product_amount_is_null)
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
        } else { basket.add(p) }
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
