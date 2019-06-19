package com.together.chat.anyidea

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.together.R
import com.together.app.UiState

class AnyIdeaAdapter : RecyclerView.Adapter<IdeaViewHolder>() {

    val data: MutableList<UiState.ChatMessage> = mutableListOf()

    fun addMessage(msg: UiState.ChatMessage){
        data.add(msg)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder {
        // diff on ViewTyp
        return IdeaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_message, parent, false)
        )
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        holder.bindItem(position, data[position])
    }


}





