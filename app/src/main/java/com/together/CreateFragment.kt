package com.together

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.together.app.UiState
import kotlinx.android.synthetic.main.fragment_create.*
import java.io.File
import java.io.IOException
import java.util.*


class CreateFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var newProduct: UiState.Article? = UiState.Article()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        create_fab.setOnClickListener {
            startActivityForResult(getPhotoIntent(), 12)
        }
    }

    private fun getPhotoIntent(): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePicture ->
            takePicture.resolveActivity(context!!.packageManager)
                ?.also {
                    val file: File? = try {
                        File(context!!.filesDir, UUID.randomUUID().toString())
                    } catch (ex: IOException) {
                        throw ex
                    }
                    file?.also {
                        val fileUri = FileProvider.getUriForFile(context!!, context!!.packageName, file)
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.e("TTTTT", "For debugging")
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
}
