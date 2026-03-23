package com.example.cabshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.TripAdapter
import com.example.cabshare.databinding.ActivityFindTripBinding
import com.example.cabshare.model.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class FindTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindTripBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: TripAdapter
    private var allTrips = mutableListOf<Trip>()
    private var tripListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        startListeningForTrips()

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchDestination.text.toString().trim()
            filterTrips(query)
        }
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(
            trips = mutableListOf(),
            onJoinClick = { trip ->
                joinTrip(trip)
            },
            onChatClick = { trip ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiverId", trip.userId)
                intent.putExtra("receiverName", trip.userName)
                startActivity(intent)
            }
        )
        binding.rvTrips.layoutManager = LinearLayoutManager(this)
        binding.rvTrips.adapter = adapter
    }

    private fun joinTrip(trip: Trip) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        showLoading(true)
        val tripRef = db.collection("trips").document(trip.tripId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(tripRef)
            val availableSeats = snapshot.getLong("availableSeats") ?: 0
            val passengers = snapshot.get("passengers") as? List<*> ?: emptyList<String>()
            
            if (availableSeats > 0 && !passengers.contains(currentUserId)) {
                transaction.update(tripRef, "availableSeats", availableSeats - 1)
                transaction.update(tripRef, "passengers", FieldValue.arrayUnion(currentUserId))
                null
            } else {
                throw Exception("No seats available or already joined")
            }
        }.addOnSuccessListener {
            showLoading(false)
            Toast.makeText(this, "Successfully joined the trip!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            showLoading(false)
            Toast.makeText(this, "Failed to join: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startListeningForTrips() {
        showLoading(true)
        tripListener = db.collection("trips")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                showLoading(false)
                if (e != null) {
                    Toast.makeText(this, "Error loading trips: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    allTrips.clear()
                    for (document in snapshot.documents) {
                        val trip = document.toObject(Trip::class.java)
                        if (trip != null) {
                            allTrips.add(trip)
                        }
                    }
                    val query = binding.etSearchDestination.text.toString().trim()
                    filterTrips(query)
                }
            }
    }

    private fun filterTrips(query: String) {
        val filteredList = if (query.isNotEmpty()) {
            allTrips.filter { 
                it.destination.contains(query, ignoreCase = true) || 
                it.startingLocation.contains(query, ignoreCase = true)
            }
        } else {
            allTrips
        }
        updateUI(filteredList)
    }

    private fun updateUI(trips: List<Trip>) {
        adapter.updateList(trips)
        if (trips.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvTrips.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvTrips.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        tripListener?.remove()
    }
}
