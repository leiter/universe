package com.together.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.together.R
import com.together.base.*
import com.together.order.ProductAdapter
import com.together.repository.Database
import com.together.repository.Result
import com.together.repository.storage.getObservable
import com.together.utils.FileUtil
import com.together.utils.dataArticleToUi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_create.*
import java.io.File
import java.io.FileOutputStream

class CreateFragment : BaseFragment(), ProductAdapter.ItemClicked {


    private lateinit var adapter: ProductAdapter

    private lateinit var picasso: Picasso

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    companion object {
        const val TAG = "CreateFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        picasso = Picasso.Builder(context).downloader(OkHttp3Downloader(context)).build()

        viewModel.newProduct.observe(viewLifecycleOwner, Observer {
            picasso.load(it.uri).into(image)
        })
        viewModel.editProduct.observe(viewLifecycleOwner, Observer {
            product_name.setText(it.productName)
            product_description.setText(it.productDescription)
            product_price.setText(it.price)
            product_price_unit.setText(it.unit)
            article_available.isChecked = it.available
            viewModel.newProduct.value = UiState.NewProductImage(Uri.parse(it.remoteImageUrl))
        })

        title.text = "Mein Angebot"

        toolbar_end_2.setOnClickListener { viewModel.deleteProduct() }

        adapter = ProductAdapter(this)
        product_list.layoutManager = LinearLayoutManager(context)
        product_list.adapter = adapter

        val products = Database.articles()
        disposable.add(products.getObservable<Result.Article>()
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.addItem(it.dataArticleToUi())
            if(adapter.data.size==0){
                empty_message.visibility = View.VISIBLE
            } else                 {
                empty_message.visibility = View.GONE
            }
        })

        create_fab.setOnClickListener {
            createBitmap(viewModel.newProduct.value!!.uri)
        }

        save_changes.setOnClickListener {
            createBitmap(viewModel.newProduct.value!!.uri)
        }

        manage_image.setOnClickListener {
            UtilsActivity.startAddImage(requireActivity())
        }

        toolbar_start.setImageResource(R.drawable.ic_menu_hamburger)
        toolbar_start.setOnClickListener {
           MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START))
        }
    }

    override fun clicked(item: UiState.Article) {
        viewModel.editProduct.value = item
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MainMessagePipe.uiEvent.onNext(UiEvent.UnlockDrawer)
        disposable.clear()
    }

    private fun updateProduct(imageUri: Uri) {

        writeToNewProduct()
        FirebaseStorage.getInstance()
            .reference
            .child("images/${System.currentTimeMillis()}_${imageUri.lastPathSegment}")
            .putFile(imageUri)
            .addOnSuccessListener {

                it.metadata?.reference?.downloadUrl?.addOnSuccessListener { imageUri ->

                    val uiState = viewModel.editProduct.value!!

                    val result = Result.Article(
                        id = "",
                        productId = -1,
                        productName = uiState.productName,
                        productDescription = uiState.productDescription,
                        imageUrl = imageUri.toString(),
                        available = uiState.available,
                        price = uiState.price.replace(",",".").toDouble(),
                    )
                    if(uiState._id.isNotEmpty()) {
                        val m = mutableMapOf( "price" to 7.8 as Any)
                        Database.updateArticle(uiState._id).updateChildren(m)
                    } else {
                        Database.articles().push().setValue(result)
                    }
                }
            }

    }


    private fun createBitmap(imageUri: Uri) {
        disposable.add(Observable.just(Any()).map {
            val bitmap: Bitmap = Bitmap.createBitmap(
                image.width, image.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            image.draw(canvas)
            val tmpFile = createTempFile()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(tmpFile))
            tmpFile
        }.subscribe {
                updateProduct(Uri.fromFile(it))
            imageUri.path?.let { file -> FileUtil.deleteFile(File(file)) }

            })
    }


    private fun writeToNewProduct() {
        viewModel.editProduct.value?.productName = product_name.text.toString()
        viewModel.editProduct.value?.productDescription = product_description.text.toString()
        viewModel.editProduct.value?.pricePerUnit = product_price.text.toString()
        viewModel.editProduct.value?.price = product_price.text.toString()
        viewModel.editProduct.value?.unit = product_price_unit.text.toString()
        viewModel.editProduct.value?.available = article_available.isChecked
    }


}
