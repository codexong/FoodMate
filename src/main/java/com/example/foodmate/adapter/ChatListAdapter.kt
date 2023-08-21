package com.example.foodmate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmate.R
import com.example.foodmate.databinding.ItemChatlistBinding
import com.example.foodmate.model.MeetingDto
import okhttp3.ResponseBody

class ChatListViewHolder(val binding: ItemChatlistBinding) : RecyclerView.ViewHolder(binding.root)

class ChatListAdapter(private val context: Context, private var meetingList: MutableList<MeetingDto>) :
    RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChatlistBinding.inflate(inflater, parent, false)
        return ChatListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val meeting = meetingList[position]
        holder.bind(meeting)
    }

    override fun getItemCount(): Int {
        return meetingList.size
    }

    inner class ChatListViewHolder(private val binding: ItemChatlistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(meeting: MeetingDto) {
            binding.chattitle.text = meeting.meeting_title
            binding.chatcontents.text = meeting.meeting_content
            binding.chatuser.text = meeting.user.toString()
        }

    }

    fun setData(meetingList: List<MeetingDto>) {
        this.meetingList = meetingList.toMutableList()
        notifyDataSetChanged()
    }

}

