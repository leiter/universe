package com.together.chat.anyidea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.app.UiState
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.chat_message.*

class AnyIdeaAdapter(val data: MutableList<UiState.PostChatMessage>) :
    RecyclerView.Adapter<AnyIdeaAdapter.IdeaViewHolder>() {

    fun update(newData: MutableList<UiState.PostChatMessage>) {
        //todo use diffutils
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder {
        // diff on ViewTyp
        return IdeaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_message, parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        holder.bindItem(position)
    }


    inner class IdeaViewHolder(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem(position: Int) {
            message_creator.text = data[position].name
            message_text.text = data[position].text
        }
    }

}



