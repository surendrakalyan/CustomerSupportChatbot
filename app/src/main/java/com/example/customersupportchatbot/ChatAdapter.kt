package com.example.customersupportchatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: MutableList<Message>
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val messageText: TextView =
            view.findViewById(R.id.messageText)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {

        val layout = if (viewType == 1) {
            R.layout.item_user_message
        } else {
            R.layout.item_bot_message
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int
    ) {
        holder.messageText.text =
            messages[position].text
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) {
            1
        } else {
            0
        }
    }
}