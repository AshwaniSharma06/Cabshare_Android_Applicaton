package com.example.cabshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.TripAdapter
import com.example.cabshare.databinding.ActivityMyTripsBinding
import com.example.cabshare.model.Trip
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyTripsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyTripsBinding
    private lateinit var adapter: TripAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyTripsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        setupTabs()
        fetchTrips(true) // Initial fetch for "Offering" (Created by user)
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(
            trips = emptyList(),
            onJoinClick = { /* No join needed here usually */ },
            onChatClick = { trip ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("tripId", trip.tripId)
                intent.putExtra("destination", trip.destination)
                startActivity(intent)
            }
        )
        binding.rvMyTrips.layoutManager = LinearLayoutManager(this)
        binding.rvMyTrips.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> fetchTrips(true)  // Offering
                    1 -> fetchTrips(false) // Joined
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun fetchTrips(isOffering: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        
        val query = if (isOffering) {
            db.collection("trips")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            db.collection("trips")
                .whereArrayContains("passengers", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        }

        query.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            val trips = value?.toObjects(Trip::class.java) ?: emptyList()
            adapter.updateList(trips)
            
            if (trips.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvMyTrips.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvMyTrips.visibility = View.VISIBLE
            }
        }
    }
}
