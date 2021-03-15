package com.together.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.widget.textChanges
import com.together.R
import com.together.base.*
import com.together.base.UiEvent.Companion.DELETE_PRODUCT
import com.together.databinding.FragmentCreateBinding
import com.together.dialogs.InfoDialogFragment
import com.together.repository.Result
import com.together.utils.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class CreateFragment : BaseFragment(R.layout.fragment_create), ProductAdapter.ItemClicked,
    View.OnFocusChangeListener {

    private lateinit var productData: List<UiState.Article>

    private lateinit var adapter: ProductAdapter
    private val viewBinding: FragmentCreateBinding by viewLifecycleLazy {
        FragmentCreateBinding.bind(requireView())
    }

    companion object {
        const val TAG = "CreateFragment"
    }

    private fun makeEditable(edit: Boolean) {
        with(viewBinding) {
            productSearchTerm.isEnabled = edit
            productName.isEnabled = edit
            productPrice.isEnabled = edit
            productPriceUnit.isEnabled = edit
            productNumber.isEnabled = edit
            manageImage.isEnabled = edit
            btnEditInfo.isEnabled = edit
            swAvailable.isEnabled = edit
            etProductWeigh.isEnabled = edit
            etProductCategory.isEnabled = edit
            changePicture.isEnabled = edit
        }
    }

    private fun resetProduct() {
        viewModel.uploadImage = false
        with(viewBinding) {
            productSearchTerm.setText("")
            productName.setText("")
            productPrice.setText("")
            productPriceUnit.setText("")
            productNumber.setText("")
            etProductWeigh.setText("")
            etProductCategory.setText("")
            image.setImageBitmap(null)
        }
    }

    private val saveClick: (View) -> Unit = lambda@{
        if (!writeToNewProduct()) {
            return@lambda
        }
        if (viewModel.editProduct.value?.remoteImageUrl.isNullOrEmpty() && !viewModel.uploadImage) {
            requireContext().showLongToast("Bild hinzufügen.")
            return@lambda
        }
        requireContext().showAlertDialog(
            "Speichern",
            title = "Speichern",
            message = "Soll das Produkt wirklich gespeichert werden?",
            actionOnPositiveButton = ::createBitmap
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ProductAdapter(this)
        with(viewBinding) {
            etFilterProducts.onFocusChangeListener = this@CreateFragment
            saveProduct.setOnClickListener(saveClick)
            productName.textChanges().skipInitialValue().subscribe {
                val t = viewModel.editProduct.value!!.prepareSearchTerms()
                productSearchTerm.setText(t)
            }.addTo(disposable)
            btnDeleteProduct.setOnClickListener {
                if (viewModel.editProduct.value?.id == "") {
                    requireContext().showShortToast("Es ist kein Produkt ausgewählt.")
                    return@setOnClickListener
                }
                requireContext().showAlertDialog(
                    "Löschen",
                    message = "Soll das Produkt wirklich gelöscht werden?",
                    actionOnPositiveButton = ::deleteProduct,
                    title = "Löschen"
                )
            }
            manageImage.setOnClickListener { UtilsActivity.startAddImage(requireActivity()) }
            changePicture.setOnClickListener { UtilsActivity.startAddImage(requireActivity()) }
            btnDrawerOpen.setOnClickListener {
                MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START))
            }

            btnClearSearch.setOnClickListener { viewBinding.etFilterProducts.clearFocus() }
            btnEditInfo.setOnClickListener { showDetailInfo() }

            btnCreateNewProduct.setOnClickListener {
                viewModel.editProduct.value = UiState.Article()
                makeEditable(true)
            }

            btnEditProduct.setOnClickListener { makeEditable(true) }

            productList.layoutManager = LinearLayoutManager(context)
            productList.adapter = adapter
        }

        viewModel.newProduct.observe(viewLifecycleOwner, {
            requireContext().loadImage(viewBinding.image, it.uri.toString())
        })
        viewModel.editProduct.observe(viewLifecycleOwner, {
            resetProduct()
            makeEditable(false)
            with(viewBinding) {
                productName.setText(it.productName)
                productSearchTerm.setText(it.searchTerms)
                productPrice.setText(it.pricePerUnit)
                productPriceUnit.setText(it.unit)
                etProductCategory.setText(it.category)
                productNumber.setText(it.productId)
                swAvailable.isChecked = it.available
                val weight = if (it.weightPerPiece == 0.0) "" else it.weightPerPiece.toString()
                etProductWeigh.setText(weight)
            }

            if (it.remoteImageUrl.isEmpty()) {
                viewBinding.manageImage.visibility = View.VISIBLE
                viewBinding.changePicture.visibility = View.GONE
            } else {
                viewBinding.changePicture.visibility = View.VISIBLE
                viewBinding.manageImage.visibility = View.GONE
                viewModel.newProduct.value = UiState.NewProductImage(
                    Uri.parse(it.remoteImageUrl), Result.UNDEFINED
                )
            }
        })

        viewModel.blockingLoaderState.observe(viewLifecycleOwner, {
            if (it is UiEvent.LoadingDone) {
                viewBinding.loadingCreate.visibility = View.GONE
                if (it.contextId == DELETE_PRODUCT) {
                    resetProduct()
                }

            } else if (it is UiEvent.Loading) {
                viewBinding.loadingCreate.visibility = View.VISIBLE
            } else {
                viewBinding.loadingCreate.visibility = View.GONE
            }
        })

        viewModel.productList.observe(viewLifecycleOwner, {
            if (it.size > 0) {
                viewBinding.emptyMessage.remove()
                adapter.setFilteredList(it.toMutableList())
            } else {
                viewBinding.emptyMessage.show()
            }
        })

        viewBinding.etFilterProducts.textChanges().skipInitialValue()
            .debounce(400, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .map { searchTerm ->
                if (!::productData.isInitialized) {
                    productData = adapter.data.toMutableList()
                }
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
            .subscribe { filtered -> adapter.setFilteredList(filtered.toMutableList()) }
            .addTo(disposable)
    }

    private fun deleteProduct() {
        viewModel.deleteProduct()
    }

    override fun clicked(item: UiState.Article) {
        viewModel.editProduct.value = item
    }

    private fun createBitmap() {
        viewModel.uploadProduct(Single.fromCallable {
            val bitmap: Bitmap = Bitmap.createBitmap(
                viewBinding.image.width, viewBinding.image.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            viewBinding.image.draw(canvas)
            val tmpFile = File.createTempFile("img", "trash")
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(tmpFile))
            tmpFile
        })
    }

    private fun writeToNewProduct(): Boolean {

        with(viewModel.editProduct) {
            if (viewBinding.productName.validate(::validString, "Produktnamen eingeben") == "") {
                return false
            } else {
                value?.productName = viewBinding.productName.text.toString().trim()
            }
            if (viewBinding.productSearchTerm.validate(
                    ::validString,
                    "Suchbegriffe eingeben."
                ) == ""
            ) {
                return false
            } else {
                value?.searchTerms = viewBinding.productSearchTerm.text.toString().trim()
            }
            if (viewBinding.productPrice.validate(::validString, "Preis eingeben.") == "") {
                return false
            } else {
                value?.pricePerUnit = viewBinding.productPrice.text.toString().trim()
            }
            if (viewBinding.productPriceUnit.validate(::validString, "Einheit eingeben.") == "") {
                return false
            } else {
                value?.unit = viewBinding.productPriceUnit.text.toString().trim()
            }

//            if (viewBinding.etProductWeigh.validate(
//                    ::validString,
//                    "Einzelgewicht eingeben."
//                ) == ""
//            ) {
//                return false
//            } else {
            val v = viewBinding.etProductWeigh.text.toString()
                 if(v.isNotEmpty()) {
                     value?.weightPerPiece = v.replace(",", ".").trim().toDouble()
                 }
//            }

            if (viewBinding.etProductCategory.validate(
                    ::validString,
                    "Kategorie eingeben."
                ) == ""
            ) {
                return false
            } else {
                value?.category = viewBinding.etProductCategory.text.toString().trim()
            }
            value?.productId = viewBinding.productNumber.text.toString()
            value?.available = viewBinding.swAvailable.isChecked
        }
        return true
    }

    private fun showDetailInfo() {
        InfoDialogFragment.newInstance(
            InfoDialogFragment.EDIT_INFO,
            viewModel.editProduct.value!!.detailInfo
        ).show(childFragmentManager, InfoDialogFragment.TAG)
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v?.id == R.id.et_filter_products) {
            if (hasFocus.not()) {
                with(viewBinding){
                    btnCreateNewProduct.show()
                    btnClearSearch.remove()
                }
                viewBinding.etFilterProducts.setText("")
            } else {
                with(viewBinding){
                    btnCreateNewProduct.remove()
                    btnClearSearch.show()
                }
            }
        }
    }

}
