package com.together.order.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.repository.TestData
import kotlinx.android.synthetic.main.main_order_fragment.*

class OrderFragmentMain : Fragment() {


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_order_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        article_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val d = TestData.uiArticleList.toMutableList()
        article_list.adapter = ArticleAdapter(d)
//
//        MainMessagePipe.mainThreadMessage.subscribe {
//            when (it) {
//                is Result.Article -> {
//                    val s = (it).productName
//                    Toast.makeText(
//                        baseContext, "msg Received ${s}.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//            }
//        }

    }


}
