package com.example.cabshare.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cabshare.R
import com.example.cabshare.databinding.ItemTripBinding
import com.example.cabshare.model.Trip
import com.example.cabshare.ui.TripDetailActivity
import com.google.firebase.auth.FirebaseAuth

class TripAdapter(
    private var trips: List<Trip>,
    private val onJoinClick: (Trip) -> Unit,
    private val onChatClick: (Trip) -> Unit,
    private val onDeleteClick: ((Trip) -> Unit)? = null
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class TripViewHolder(val binding: ItemTripBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        val context = holder.itemView.context

        holder.binding.tvTripStarting.text = trip.startingLocation
        holder.binding.tvTripDestination.text = trip.destination
        holder.binding.tvTripUser.text = trip.userName
        holder.binding.tvUserRating.text = String.format("%.1f", trip.userRating)
        holder.binding.tvTripDate.text = trip.date
        holder.binding.tvTripTime.text = trip.time
        holder.binding.tvTripFare.text = "₹${trip.fare}"
        holder.binding.tvTripSeats.text = "${trip.availableSeats} seats available"

        // Preferences Badges
        holder.binding.tvACBadge.visibility = if (trip.isAC) View.VISIBLE else View.GONE
        holder.binding.tvGenderBadge.text = "Gender: ${trip.genderPreference}"

        // Load User Image
        Glide.with(context)
            .load(trip.userProfileImage)
            .placeholder(R.drawable.ic_default_profile)
            .error(R.drawable.ic_default_profile)
            .circleCrop()
            .into(holder.binding.ivUserImage)

        // Logic for Join Button
        val isFull = trip.availableSeats <= 0
        val isAlreadyJoined = trip.passengers.contains(currentUserId)
        val isMyTrip = trip.userId == currentUserId

        holder.binding.btnJoinTrip.isEnabled = !isFull && !isAlreadyJoined && !isMyTrip
        
        // Delete button visibility
        if (isMyTrip && onDeleteClick != null) {
            holder.binding.btnDeleteTrip.visibility = View.VISIBLE
            holder.binding.btnDeleteTrip.setOnClickListener {
                onDeleteClick.invoke(trip)
            }
        } else {
            holder.binding.btnDeleteTrip.visibility = View.GONE
        }

        when {
            isMyTrip -> {
                holder.binding.btnJoinTrip.text = "My Trip"
                holder.binding.btnJoinTrip.isEnabled = false
            }
            isAlreadyJoined -> {
                holder.binding.btnJoinTrip.text = "Joined"
                holder.binding.btnJoinTrip.isEnabled = false
            }
            isFull -> {
                holder.binding.btnJoinTrip.text = "Full"
                holder.binding.btnJoinTrip.isEnabled = false
            }
            else -> {
                holder.binding.btnJoinTrip.text = "Join"
                holder.binding.btnJoinTrip.isEnabled = true
            }
        }

        holder.binding.btnJoinTrip.setOnClickListener {
            onJoinClick(trip)
        }

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, TripDetailActivity::class.java)
            intent.putExtra("tripId", trip.tripId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = trips.size

    fun updateList(newList: List<Trip>) {
        trips = newList
        notifyDataSetChanged()
    }
}
