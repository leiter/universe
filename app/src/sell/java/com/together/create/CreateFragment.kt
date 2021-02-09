package com.together.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.together.R
import com.together.base.*
import com.together.base.UiEvent.Companion.DELETE_PRODUCT
import com.together.databinding.FragmentCreateBinding
import com.together.repository.Database
import com.together.repository.Result
import com.together.utils.FileUtil
import com.together.utils.loadImage
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.io.File
import java.io.FileOutputStream

class CreateFragment : BaseFragment(R.layout.fragment_create), ProductAdapter.ItemClicked {

    private lateinit var adapter: ProductAdapter
    private var vB: FragmentCreateBinding? = null
    private val viewBinding: FragmentCreateBinding
        get() = vB!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        vB = FragmentCreateBinding.inflate(inflater, container, false)

        adapter = ProductAdapter(this)

        with(viewBinding) {

            saveChanges.setOnClickListener { createBitmap(viewModel.newProduct.value!!.uri) }

            btnDeleteProduct.setOnClickListener { viewModel.deleteProduct() }

            createFab.setOnClickListener { createBitmap(viewModel.newProduct.value!!.uri) }

            manageImage.setOnClickListener { UtilsActivity.startAddImage(requireActivity()) }

//            btnDrawerOpen.setOnClickListener {
//                MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START))
//            }

            createNewProduct.visibility = View.VISIBLE
            createNewProduct.setOnClickListener { makeEditable(true) }

            productList.layoutManager = LinearLayoutManager(context)
            productList.adapter = adapter
        }
        return viewBinding.root
    }

    companion object {
        const val TAG = "CreateFragment"
    }

    private fun makeEditable(edit: Boolean) {
        viewBinding.productDescription.isEnabled = edit
        viewBinding.productName.isEnabled = edit
        viewBinding.productPrice.isEnabled = edit
        viewBinding.productPriceUnit.isEnabled = edit
        viewBinding.manageImage.isEnabled = edit
    }

    private fun resetProduct() {
        viewBinding.productDescription.setText("")
        viewBinding.productName.setText("")
        viewBinding.productPrice.setText("")
        viewBinding.productPriceUnit.setText("")
        viewBinding.image.setImageBitmap(null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
//                viewBinding.changePicture.visibility = View.VISIBLE
            }
        })

        viewModel.blockingLoaderState.observe(viewLifecycleOwner, {
            if (it is UiEvent.LoadingDone) {
                viewBinding.loadingIndicator.visibility = View.GONE
                if (it.indicator == DELETE_PRODUCT) {
                    resetProduct()
                }

            } else {
                viewBinding.loadingIndicator.visibility = View.VISIBLE
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
        if (uiState._id.isNotEmpty()) {
            val m = mutableMapOf("price" to 7.8 as Any)
            Database.updateArticle(uiState._id).updateChildren(m)
        } else {
            Database.articles().push().setValue(result)
        }
        return
        FirebaseStorage.getInstance()
            .reference
            .child("images/${System.currentTimeMillis()}_${imageUri.lastPathSegment}")
            .putFile(imageUri)
            .addOnSuccessListener {

                it.metadata?.reference?.downloadUrl?.addOnSuccessListener { imageUri ->

                    val uiState = viewModel.editProduct.value!!

                    val result = Result.Article(
                        productName = uiState.productName,
                        imageUrl = imageUri.toString(),
                        unit = viewBinding.productPriceUnit.toString(),
                        available = uiState.available,
                        price = uiState.pricePerUnit.replace("€", "")
                            .replace(",", ".").toDouble()
                    )
                    if (uiState._id.isNotEmpty()) {
                        val m = mutableMapOf("price" to 7.8 as Any)
                        Database.updateArticle(uiState._id).updateChildren(m)
                    } else {
                        Database.articles().push().setValue(result)
                    }
                }
            }
    }


    private fun createBitmap(imageUri: Uri) {
        Observable.just(Any()).map {
            val bitmap: Bitmap = Bitmap.createBitmap(
                viewBinding.image.width, viewBinding.image.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            viewBinding.image.draw(canvas)
            val tmpFile = File.createTempFile("img", "trash")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(tmpFile))
            tmpFile
        }.subscribe {
            updateProduct(Uri.fromFile(it))
            imageUri.path?.let { file -> FileUtil.deleteFile(File(file)) }
        }.addTo(disposable)
    }


    private fun writeToNewProduct() {
        viewModel.editProduct.value?.productName = viewBinding.productName.text.toString()
        viewModel.editProduct.value?.productDescription =
            viewBinding.productDescription.text.toString()
        viewModel.editProduct.value?.pricePerUnit = viewBinding.productPrice.text.toString()
        viewModel.editProduct.value?.unit = viewBinding.productPriceUnit.text.toString()
        viewModel.editProduct.value?.available = viewBinding.articleAvailable.isChecked
    }

    override fun onDestroyView() {
        vB = null
        disposable.clear()
        super.onDestroyView()
    }

}
