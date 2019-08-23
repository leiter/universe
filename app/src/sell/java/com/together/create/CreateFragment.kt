package com.together.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
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
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fake_toolbar.*
import kotlinx.android.synthetic.main.fragment_create.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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

        model = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        model.newProduct.observe(this, Observer {
            picasso.load(it.uri).into(image)
        })
        model.editProduct.observe(viewLifecycleOwner, Observer {//todo do not listen and write
            product_name.setText(it.productName)
            product_description.setText(it.productDescription)
            product_price.setText(it.pricePerUnit)
            product_price_unit.setText(it.unit)
            article_available.isChecked = it.available
            product_discount.setText(it.discount.toString())
            model.newProduct.value = UiState.NewProductImage(Uri.parse(it.remoteImageUrl))
        })

        toolbar_end_1.setImageResource(R.drawable.ic_edit_black)
        toolbar_end_1.visibility = View.VISIBLE


        toolbar_end_2.setImageResource(R.drawable.ic_delete)
        toolbar_end_2.visibility = View.VISIBLE
        toolbar_end_2.setOnClickListener {
            val id = model.editProduct.value?.id
            if (id != null) deleteArticle(id)
        }

        product_list.layoutManager = LinearLayoutManager(context)

        adapter = ProductAdapter(this)
        product_list.adapter = adapter

        val ref = FirebaseDatabase.getInstance().reference
        val products = Database.articles() //ref.child("articles")
        disposable.add(products.getObservable<Result.Article>().subscribe {
            val e = UiState.Article(
                id = it.id,
                productName = it.productName,
                productDescription = it.productDescription,
                remoteImageUrl = it.imageUrl,
                unit = it.unit,
                pricePerUnit = it.pricePerUnit,
                discount = it.discount,
                mode = it.mode,
                available = it.available
            )

            adapter.addItem(e)
            if(adapter.data.size==0){
                empty_message.visibility = View.VISIBLE
            } else                 {
                empty_message.visibility = View.GONE
            }

        })

        create_fab.setOnClickListener {
            createBitmap(model.newProduct.value!!.uri)
        }

        manage_image.setOnClickListener {
            UtilsActivity.startAddImage(activity!!)
        }

        toolbar_start.setImageResource(R.drawable.ic_menu_hamburger)
        toolbar_start.setOnClickListener {
           MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START))
        }

    }


    override fun clicked(item: UiState.Article) {
        model.editProduct.value = item
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
            .child("images/${imageUri.lastPathSegment}")
            .putFile(imageUri)
            .addOnSuccessListener {

                it.metadata?.reference?.downloadUrl?.addOnSuccessListener {

                    val uiState = model.editProduct.value!!

                    val resulT = Result.Article(
                        id = "",
                        productId = -1,
                        productName = uiState.productName,
                        productDescription = uiState.productDescription,
                        imageUrl = it.toString(),
                        available = uiState.available,
                        pricePerUnit = uiState.pricePerUnit,
                        unit = uiState.unit,
                        discount = uiState.discount


                    )
//                    val fireData = FireData()
                    Database.articles().push().setValue(resulT)
//                    FireData.createDocument(FirebaseDatabase.getInstance().reference, "articles", resulT)
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
        }
            .subscribe {
                updateProduct(Uri.fromFile(it))
                FileUtil.deleteFile(File(imageUri.path))

            })
    }

    private fun getRotation(inputStream: InputStream): Int {
        val exif = ExifInterface(inputStream)
        val result = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val rotation: Int
        rotation = when (result) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        return rotation
    }

    private fun writeToNewProduct() {
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
