package com.example.cabshare.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.TripAdapter
import com.example.cabshare.databinding.ActivityFindTripBinding
import com.example.cabshare.model.Trip
import com.google.firebase.firestore.FirebaseFirestore

class FindTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindTripBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: TripAdapter
    private var allTrips = mutableListOf<Trip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchTrips()

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchDestination.text.toString().trim()
            if (query.isNotEmpty()) {
                val filteredList = allTrips.filter { 
                    it.destination.contains(query, ignoreCase = true) 
                }
                adapter.updateList(filteredList)
            } else {
                adapter.updateList(allTrips)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(mutableListOf()) { trip ->
            // Step 9: Open Chat with user
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverId", trip.userId)
            intent.putExtra("receiverName", trip.userName)
            startActivity(intent)
        }
        binding.rvTrips.layoutManager = LinearLayoutManager(this)
        binding.rvTrips.adapter = adapter
    }

    private fun fetchTrips() {
        db.collection("trips").get()
            .addOnSuccessListener { documents ->
                allTrips.clear()
                for (document in documents) {
                    val trip = document.toObject(Trip::class.java)
                    allTrips.add(trip)
                }
                adapter.updateList(allTrips)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load trips", Toast.LENGTH_SHORT).show()
            }
    }
}
