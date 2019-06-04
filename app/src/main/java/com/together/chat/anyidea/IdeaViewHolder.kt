package com.together.chat.anyidea

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.together.app.UiState
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.chat_message.*

class IdeaViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bindItem(position: Int, item: UiState.ChatMessage) {
        message_creator.text = item.name
        message_text.text = item.text
    }
}