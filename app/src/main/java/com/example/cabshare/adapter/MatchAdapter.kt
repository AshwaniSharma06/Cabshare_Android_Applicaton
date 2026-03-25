package com.example.cabshare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cabshare.R
import com.example.cabshare.databinding.ItemMatchBinding
import com.example.cabshare.model.Match
import com.google.firebase.auth.FirebaseAuth

class MatchAdapter(
    private var matches: List<Match>,
    private val onChatClick: (Match) -> Unit
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class MatchViewHolder(val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        
        // Determine which user in the match is the "other" user
        val isUser1 = match.userId1 == currentUserId
        val otherUserName = if (isUser1) match.userName2 else match.userName1
        val otherUserImage = if (isUser1) match.userImage2 else match.userImage1
        val otherLocation = if (isUser1) match.location2 else match.location1

        holder.binding.tvOtherUserName.text = otherUserName
        holder.binding.tvMatchLocation.text = "Pickup: $otherLocation"
        holder.binding.tvMatchDate.text = "Trip Date: ${match.date}"

        Glide.with(holder.itemView.context)
            .load(otherUserImage)
            .placeholder(R.drawable.ic_default_profile)
            .circleCrop()
            .into(holder.binding.ivOtherUser)

        holder.binding.btnChat.setOnClickListener {
            onChatClick(match)
        }
    }

    override fun getItemCount() = matches.size

    fun updateMatches(newMatches: List<Match>) {
        matches = newMatches
        notifyDataSetChanged()
    }
}
