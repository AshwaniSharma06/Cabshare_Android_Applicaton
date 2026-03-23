package com.example.cabshare.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cabshare.databinding.ItemTripBinding
import com.example.cabshare.model.Trip
import com.example.cabshare.ui.TripDetailActivity
import com.google.firebase.auth.FirebaseAuth

class TripAdapter(
    private var trips: List<Trip>,
    private val onJoinClick: (Trip) -> Unit,
    private val onChatClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class TripViewHolder(val binding: ItemTripBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.binding.tvTripStarting.text = trip.startingLocation
        holder.binding.tvTripDestination.text = trip.destination
        holder.binding.tvTripUser.text = "Posted by: ${trip.userName}"
        holder.binding.tvTripDate.text = trip.date
        holder.binding.tvTripTime.text = trip.time
        holder.binding.tvTripFare.text = "Fare: ₹${trip.fare}"
        holder.binding.tvTripSeats.text = "Seats: ${trip.availableSeats}/${trip.totalSeats}"

        // Logic for Join Button
        val isFull = trip.availableSeats <= 0
        val isAlreadyJoined = trip.passengers.contains(currentUserId)
        val isMyTrip = trip.userId == currentUserId

        holder.binding.btnJoinTrip.isEnabled = !isFull && !isAlreadyJoined && !isMyTrip
        
        when {
            isMyTrip -> {
                holder.binding.btnJoinTrip.text = "My Trip"
            }
            isAlreadyJoined -> {
                holder.binding.btnJoinTrip.text = "Joined"
            }
            isFull -> {
                holder.binding.btnJoinTrip.text = "Full"
            }
            else -> {
                holder.binding.btnJoinTrip.text = "Join Trip"
            }
        }

        holder.binding.btnJoinTrip.setOnClickListener {
            onJoinClick(trip)
        }

        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
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
