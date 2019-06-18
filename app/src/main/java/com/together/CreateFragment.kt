package com.together

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.together.addpicture.AddPictureImpl
import com.together.app.UiState
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_create.*


class CreateFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private val disposable = CompositeDisposable()
    private val addPicture = AddPictureImpl(this)

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
            disposable.add(addPicture.startAddPhoto())
        }
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
}
