package com.example.cabshare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cabshare.R
import com.example.cabshare.databinding.ItemChatBinding
import com.example.cabshare.model.Chat
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(private val onChatClick: (Chat) -> Unit) :
    ListAdapter<Chat, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = getItem(position)
        holder.bind(chat)
        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.tvUserName.text = chat.otherUserName
            binding.tvLastMessage.text = chat.lastMessage
            
            val timestamp = chat.lastTimestamp?.toDate()
            if (timestamp != null) {
                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                binding.tvTimestamp.text = sdf.format(timestamp)
            } else {
                binding.tvTimestamp.text = ""
            }

            Glide.with(binding.ivUserImage.context)
                .load(chat.otherUserProfileImage)
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .circleCrop()
                .into(binding.ivUserImage)
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean = oldItem.chatId == newItem.chatId
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean = oldItem == newItem
    }
}
