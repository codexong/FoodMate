package com.example.foodmate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmate.R
import com.example.foodmate.databinding.ItemMessageBinding
import com.example.foodmate.model.MeetingDto
import com.example.foodmate.model.MessageDto

class ChatViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

class ChatAdapter(private val context: Context, private val messages: MutableList<MessageDto>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMessageBinding.inflate(inflater, parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun submitList(newList: List<MessageDto>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageDto) {
            binding.chatMsgLayoutBg.findViewById<TextView>(R.id.chatMsg).text = message.content
            binding.chatMsgLayoutBg.findViewById<TextView>(R.id.chatMsgUsername).text = message.nickname
            binding.chatMsgTime.text = message.time
        }

        fun bind(message: MeetingDto) {

        }
    }
}



