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
import com.together.repository.Database
import com.together.repository.Result
import com.together.utils.loadImage
import com.together.utils.viewLifecycleLazy
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream

class CreateFragment : BaseFragment(R.layout.fragment_create), ProductAdapter.ItemClicked {

    private lateinit var adapter: ProductAdapter
    private val viewBinding: FragmentCreateBinding by viewLifecycleLazy { FragmentCreateBinding.bind(requireView()) }

    companion object {
        const val TAG = "CreateFragment"
    }

    private fun makeEditable(edit: Boolean) {
        with(viewBinding){
            productDescription.isEnabled = edit
            productName.isEnabled = edit
            productPrice.isEnabled = edit
            productPriceUnit.isEnabled = edit
            manageImage.isEnabled = edit
        }
    }

    private fun resetProduct() {
        with(viewBinding){
            productDescription.setText("")
            productName.setText("")
            productPrice.setText("")
            productPriceUnit.setText("")
            image.setImageBitmap(null)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(this)
        viewBinding.image.tag = false
        with(viewBinding) {
            saveChanges.setOnClickListener { createBitmap() }
            btnDeleteProduct.setOnClickListener { viewModel.deleteProduct() }
            createFab.setOnClickListener {  }
            manageImage.setOnClickListener { UtilsActivity.startAddImage(requireActivity()) }
            btnDrawerOpen.setOnClickListener {
                MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START))
            }

            createNewProduct.visibility = View.VISIBLE
            createNewProduct.setOnClickListener { makeEditable(true) }

            btnEditProduct.setOnClickListener { }

            productList.layoutManager = LinearLayoutManager(context)
            productList.adapter = adapter
        }

        viewModel.newProduct.observe(viewLifecycleOwner, {
            requireContext().loadImage(viewBinding.image, it.uri.toString())
        })
        viewModel.editProduct.observe(viewLifecycleOwner, {
            makeEditable(false)
            viewBinding.productName.setText(it.productName)
            viewBinding.productDescription.setText(it.productDescription)
            viewBinding.productPrice.setText(it.pricePerUnit)
            viewBinding.productPriceUnit.setText(it.unit)
            viewBinding.articleAvailable.isChecked = it.available
            if (it.remoteImageUrl.isEmpty()) {
                viewBinding.changePicture.visibility = View.GONE
            } else {
                viewModel.newProduct.value = UiState.NewProductImage(Uri.parse(it.remoteImageUrl))
//                com.together.utils.viewBinding.changePicture.visibility = View.VISIBLE
            }
        })

        viewModel.blockingLoaderState.observe(viewLifecycleOwner, {
            if (it is UiEvent.LoadingDone) {
                viewBinding.loadingIndicator.visibility = View.GONE
                if (it.contextId == DELETE_PRODUCT) { resetProduct() }

            } else if(it is UiEvent.Loading) {
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

    override fun clicked(item: UiState.Article) {
        viewModel.editProduct.value = item
    }

    private fun updateProduct(imageUri: Uri) {

        writeToNewProduct()

        val uiState = viewModel.editProduct.value!!

        val result = Result.Article(
            productName = uiState.productName,
            imageUrl = imageUri.toString(),
            unit = viewBinding.productPriceUnit.text.toString(),
            available = uiState.available,
            price = uiState.pricePerUnit.replace("€", "")
                .replace(",", ".").toDouble()
        )
        if (uiState.id.isNotEmpty()) {
            val m = mutableMapOf("price" to 7.8 as Any)
            Database.updateArticle(uiState.id).updateChildren(m)
        } else {
            Database.articles().push().setValue(result)
        }
        return
    }


    private fun createBitmap() {
        writeToNewProduct()

        viewModel.uploadProduct(Single.fromCallable{
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
        with(viewModel.editProduct){
           value?.productName = viewBinding.productName.text.toString()
           value?.productDescription = viewBinding.productDescription.text.toString()
           value?.pricePerUnit = viewBinding.productPrice.text.toString()
           value?.unit = viewBinding.productPriceUnit.text.toString()
           value?.available = viewBinding.articleAvailable.isChecked
        }

    }

    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }

}
