package com.together.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.together.R


interface AnyIdeaView{

}


class AnyIdeaPresenter(val view: AnyIdeaView) {


}


class AnyIdeaFragment : Fragment(), AnyIdeaView {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.any_idea_fragment,container,false)
    }

}
