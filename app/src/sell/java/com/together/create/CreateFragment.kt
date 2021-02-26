package com.together.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.together.R
import com.together.base.*
import com.together.base.UiEvent.Companion.DELETE_PRODUCT
import com.together.databinding.FragmentCreateBinding
import com.together.dialogs.InfoDialogFragment
import com.together.utils.loadImage
import com.together.utils.showShortToast
import com.together.utils.viewLifecycleLazy
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream

class CreateFragment : BaseFragment(R.layout.fragment_create), ProductAdapter.ItemClicked {

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
            manageImage.isEnabled = edit
            btnEditInfo.isEnabled = edit
            swAvailable.isEnabled = edit
            etProductWeigh.isEnabled = edit
        }
    }

    private fun resetProduct() {
        with(viewBinding) {
            productSearchTerm.setText("")
            productName.setText("")
            productPrice.setText("")
            productPriceUnit.setText("")
            etProductWeigh.setText("")
            image.setImageBitmap(null)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(this)
        viewBinding.image.tag = false
        with(viewBinding) {
            saveProduct.setOnClickListener { createBitmap() }
            btnDeleteProduct.setOnClickListener { deleteProduct() }
//            createFab.setOnClickListener {  }
            manageImage.setOnClickListener { UtilsActivity.startAddImage(requireActivity()) }
            btnDrawerOpen.setOnClickListener {
                MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START))
            }

            btnEditInfo.setOnClickListener { showDetailInfo() }

            createNewProduct.setOnClickListener {
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
                swAvailable.isChecked = it.available
                val weight = if (it.weightPerPiece==0.0) "" else it.weightPerPiece.toString()
                etProductWeigh.setText(weight)
            }


            if (it.remoteImageUrl.isEmpty()) {
                viewBinding.changePicture.visibility = View.GONE
            } else {
                viewModel.newProduct.value = UiState.NewProductImage(Uri.parse(it.remoteImageUrl))
            }
        })

        viewModel.blockingLoaderState.observe(viewLifecycleOwner, {
            if (it is UiEvent.LoadingDone) {
                viewBinding.loadingIndicator.visibility = View.GONE
                if (it.contextId == DELETE_PRODUCT) {
                    resetProduct()
                }

            } else if (it is UiEvent.Loading) {
                viewBinding.loadingIndicator.visibility = View.VISIBLE
            } else {
                viewBinding.loadingIndicator.visibility = View.GONE
            }
        })

        viewModel.productList.observe(viewLifecycleOwner, {
            if (it.size > 0) {
                viewBinding.emptyMessage.visibility = View.GONE
                adapter.setFilteredList(it.toMutableList())
            } else {
                viewBinding.emptyMessage.visibility = View.VISIBLE
            }
        })
    }

    private fun deleteProduct() {
        if (viewModel.editProduct.value?.id != "") {
            viewModel.deleteProduct()
        } else requireContext().showShortToast("Es ist kein Produkt ausgewählt.")

    }

    override fun clicked(item: UiState.Article) {
        viewModel.editProduct.value = item
    }

    private fun createBitmap() {
        writeToNewProduct()
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
        }, viewBinding.image.tag as Boolean)
    }

    private fun writeToNewProduct() {
        with(viewModel.editProduct) {
            value?.productName = viewBinding.productName.text.toString()
            value?.productDescription = viewBinding.productName.text.toString()
            value?.productId = viewBinding.productNumber.text.toString()
            value?.weightPerPiece = viewBinding.etProductWeigh.text.toString().toDouble()
            value?.searchTerms = viewBinding.productSearchTerm.text.toString()
            value?.pricePerUnit = viewBinding.productPrice.text.toString()
            value?.unit = viewBinding.productPriceUnit.text.toString()
            value?.available = viewBinding.swAvailable.isChecked
        }
    }

    private fun showDetailInfo(){
        InfoDialogFragment.newInstance(
            InfoDialogFragment.EDIT_INFO,
            viewModel.editProduct.value!!.detailInfo
        ).show(childFragmentManager, InfoDialogFragment.TAG)
    }

}
