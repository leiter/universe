package com.together.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import com.together.utils.DataHelper
import com.together.utils.FileUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fake_toolbar.*
import kotlinx.android.synthetic.main.fragment_create.*
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat

class CreateFragment : Fragment(), ProductAdapter.ItemClicked {

    private val disposable = CompositeDisposable()

    private lateinit var model: MainViewModel

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

        model = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        model.newProduct.observe(viewLifecycleOwner, Observer {
            picasso.load(it.uri).into(image)
        })
        model.editProduct.observe(viewLifecycleOwner, Observer {// todo do not listen and write
            product_name.setText(it.productName)
            product_description.setText(it.productDescription)
            product_price.setText(it.pricePerUnit)
            product_price_unit.setText(it.unit)
            article_available.isChecked = it.available
            product_discount.setText(it.discount.toString())
            model.newProduct.value = UiState.NewProductImage(Uri.parse(it.remoteImageUrl))
        })


        title.text = "Mein Angebot"
        toolbar_end_1.setImageResource(R.drawable.ic_edit_black)
        toolbar_end_1.visibility = View.VISIBLE

        toolbar_end_2.setImageResource(R.drawable.ic_delete)
        toolbar_end_2.visibility = View.VISIBLE
        toolbar_end_2.setOnClickListener {
            val id = model.editProduct.value?._id
            if (id != null) deleteArticle(id)
        }

        product_list.layoutManager = LinearLayoutManager(context)

        adapter = ProductAdapter(this)
        product_list.adapter = adapter

        val products = Database.articles()
        disposable.add(products.getObservable<Result.Article>()
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.addItem(DataHelper.dataArticleToUi(it))
            if(adapter.data.size==0){
                empty_message.visibility = View.VISIBLE
            } else                 {
                empty_message.visibility = View.GONE
            }
        })

        create_fab.setOnClickListener {
            createBitmap(model.newProduct.value!!.uri)
        }

        save_changes.setOnClickListener {
            createBitmap(model.newProduct.value!!.uri)
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
        model.editProduct.value = item
    }

    private fun toggleEdit() {

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

                    val uiState = model.editProduct.value!!

                    val result = Result.Article(
                        id = "",
                        productId = -1,
                        productName = uiState.productName,
                        productDescription = uiState.productDescription,
                        imageUrl = imageUri.toString(),
                        available = uiState.available,

                        discount = uiState.discount
                    )
                    Database.articles().push().setValue(result)
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
                FileUtil.deleteFile(File(imageUri.path))

            })
    }


    private fun writeToNewProduct() {

        val uiState = UiState.Article(
            productName = product_name.text.toString(),
            productDescription = product_description.text.toString()
        )
        model.editProduct.value?.productName = product_name.text.toString()
        model.editProduct.value?.productDescription = product_description.text.toString()
        model.editProduct.value?.pricePerUnit = product_price.text.toString()
        model.editProduct.value?.unit = product_price_unit.text.toString()
        model.editProduct.value?.discount = NumberFormat.getInstance()
            .parse(product_discount.text.toString()).toLong()
        model.editProduct.value?.available = article_available.isChecked
    }

    private fun deleteArticle(id: String) {
        Database.articles().child(id).removeValue()
    }

}
