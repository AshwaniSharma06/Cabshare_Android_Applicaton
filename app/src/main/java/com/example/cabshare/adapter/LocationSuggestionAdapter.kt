package com.example.cabshare.adapter

import android.location.Address
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cabshare.databinding.ItemLocationSuggestionBinding

class LocationSuggestionAdapter(
    private var suggestions: List<Address>,
    private val onSuggestionClick: (Address) -> Unit
) : RecyclerView.Adapter<LocationSuggestionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLocationSuggestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLocationSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = suggestions[position]
        
        // Use featureName for the main title (e.g., "Jaipur")
        // Use getAddressLine(0) or parts of it for the full address
        holder.binding.tvLocationName.text = address.featureName ?: "Unknown Place"
        holder.binding.tvLocationAddress.text = address.getAddressLine(0)

        holder.binding.root.setOnClickListener {
            onSuggestionClick(address)
        }
    }

    override fun getItemCount() = suggestions.size

    fun updateSuggestions(newSuggestions: List<Address>) {
        suggestions = newSuggestions
        notifyDataSetChanged()
    }
}
