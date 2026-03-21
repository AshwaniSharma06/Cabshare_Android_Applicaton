package com.example.cabshare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cabshare.databinding.ItemTripBinding
import com.example.cabshare.model.Trip

class TripAdapter(private var trips: List<Trip>, private val onContactClick: (Trip) -> Unit) :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    class TripViewHolder(val binding: ItemTripBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.binding.tvTripDestination.text = trip.destination
        holder.binding.tvTripUser.text = "Posted by: ${trip.userName}"
        holder.binding.tvTripDate.text = trip.date
        holder.binding.tvTripTime.text = trip.time
        holder.binding.tvTripFare.text = "Fare: ₹${trip.fare}"

        holder.binding.btnJoinTrip.setOnClickListener {
            onContactClick(trip)
        }
    }

    override fun getItemCount(): Int = trips.size

    fun updateList(newList: List<Trip>) {
        trips = newList
        notifyDataSetChanged()
    }
}
