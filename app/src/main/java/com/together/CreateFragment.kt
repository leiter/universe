package com.together

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.together.app.*
import com.together.order.main.ProductAdapter
import com.together.repository.Result
import com.together.repository.storage.FireData
import com.together.repository.storage.getObservable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fake_toolbar.*
import kotlinx.android.synthetic.main.fragment_create.*


class CreateFragment : Fragment(), ProductAdapter.ItemClicked {

    override fun clicked(item: UiState.Article) {
        model.editProduct.value = item
    }

    private var param1: String? = null
    private var param2: String? = null

    private val disposable = CompositeDisposable()

    private lateinit var model: MainViewModel

    private lateinit var adapter: ProductAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        model.newProduct.observe(this, Observer {
            val p = Picasso.Builder(context)
                .downloader(OkHttp3Downloader(context)).build()
            p.load(it.uri)
//                .placeholder(R.drawable.ic_shopping_cart_black_24dp)
                .into(image)

        })
        model.editProduct.observe(this, Observer {
            product_name.setText(it.productName)
            product_description.setText(it.productDescription)
            product_price.setText(it.pricePerUnit)
            product_price_unit.setText(it.unit)
            model.newProduct.value = UiState.NewProductImage(Uri.parse(it.remoteImageUrl))
        })


        product_list.layoutManager = LinearLayoutManager(context)
        val d = mutableListOf<UiState.Article>()

        adapter = ProductAdapter(d, this)
        product_list.adapter = adapter

        val ref = FirebaseDatabase.getInstance().reference
        val products = ref.child("articles")
        disposable.add(products.getObservable<Result.Article>().subscribe {
            val e = UiState.Article(
                id = it.id,
                productName = it.productName,
                productDescription = it.productDescription,
                remoteImageUrl = it.imageUrl,
                unit = it.unit,
                pricePerUnit = it.pricePerUnit

            )
            adapter.addItem(e)
        })

        create_fab.setOnClickListener {
            updateProduct(model.newProduct.value!!.uri)
        }
        manage_image.setOnClickListener {
            UtilsActivity.startAddImage(activity!!)
        }

        toolbar_start.setOnClickListener { MainMessagePipe.uiEvent.onNext(UiEvent.DrawerState(Gravity.START)) }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun updateProduct(uri: Uri) {
        val ref = FirebaseStorage.getInstance().reference
            .child("images/${uri.lastPathSegment}")
            .putFile(uri)
        ref.addOnSuccessListener {

            it.metadata?.reference?.downloadUrl?.addOnSuccessListener {

                writeToNewProduct()  //writing should happen on the fly
                val uiState = model.editProduct.value!!

                val resulT = Result.Article(
                    id = "",
                    productId = -1,
                    productName = uiState.productName,
                    productDescription = uiState.productDescription,
                    imageUrl = it.toString(),
                    available = uiState.available,
                    pricePerUnit = uiState.pricePerUnit,
                    unit = uiState.unit
                )
                val fireData = FireData()
                fireData.createDocument(FirebaseDatabase.getInstance().reference, "articles", resulT)
            }

        }

    }

    private fun writeToNewProduct() {

        model.editProduct.value?.productName = product_name.text.toString()
        model.editProduct.value?.productDescription = product_description.text.toString()
        model.editProduct.value?.pricePerUnit = product_price.text.toString()
        model.editProduct.value?.unit = product_price_unit.text.toString()

    }
}
