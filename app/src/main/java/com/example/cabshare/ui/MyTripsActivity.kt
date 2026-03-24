package com.example.cabshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.TripAdapter
import com.example.cabshare.databinding.ActivityMyTripsBinding
import com.example.cabshare.model.Trip
import com.example.cabshare.viewmodel.MyTripsViewModel
import com.google.android.material.tabs.TabLayout

class MyTripsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyTripsBinding
    private val viewModel: MyTripsViewModel by viewModels()
    private lateinit var adapter: TripAdapter
    private var isHistoryMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyTripsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isHistoryMode = intent.getBooleanExtra("SHOW_HISTORY", false)
        
        setupUI()
        setupRecyclerView()
        setupTabs()
        setupObservers()

        // Initial fetch
        viewModel.fetchTrips(isOffering = true, isHistoryMode = isHistoryMode)
    }

    private fun setupUI() {
        if (isHistoryMode) {
            binding.tvTitle.text = "Booking History"
            binding.tabLayout.getTabAt(0)?.text = "Past Offers"
            binding.tabLayout.getTabAt(1)?.text = "Past Joins"
            binding.ivEmptyState.setImageResource(android.R.drawable.ic_menu_recent_history)
        } else {
            binding.tvTitle.text = "My Active Trips"
            binding.ivEmptyState.setImageResource(android.R.drawable.ic_menu_today)
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(
            trips = emptyList(),
            onJoinClick = { /* Not applicable here */ },
            onChatClick = { trip ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiverId", trip.userId)
                intent.putExtra("receiverName", trip.userName)
                startActivity(intent)
            },
            onDeleteClick = { trip ->
                if (!isHistoryMode) {
                    showDeleteConfirmation(trip)
                }
            }
        )
        binding.rvMyTrips.layoutManager = LinearLayoutManager(this)
        binding.rvMyTrips.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isOffering = tab?.position == 0
                viewModel.fetchTrips(isOffering, isHistoryMode)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupObservers() {
        viewModel.trips.observe(this) { trips ->
            adapter.updateList(trips)
            if (trips.isEmpty()) {
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.rvMyTrips.visibility = View.GONE
                binding.tvEmptyState.text = if (isHistoryMode) 
                    "No past trips found" 
                else 
                    "No active trips found"
            } else {
                binding.emptyStateContainer.visibility = View.GONE
                binding.rvMyTrips.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.deleteSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Trip cancelled successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteSuccess()
            }
        }
    }

    private fun showDeleteConfirmation(trip: Trip) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Trip")
            .setMessage("Are you sure you want to cancel this trip offer?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteTrip(trip.tripId)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
