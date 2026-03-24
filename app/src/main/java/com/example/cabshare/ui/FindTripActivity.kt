package com.example.cabshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.R
import com.example.cabshare.adapter.TripAdapter
import com.example.cabshare.databinding.ActivityFindTripBinding
import com.example.cabshare.model.Trip
import com.example.cabshare.viewmodel.TripViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FindTripActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityFindTripBinding
    private val viewModel: TripViewModel by viewModels()
    private lateinit var adapter: TripAdapter
    private var googleMap: GoogleMap? = null
    private var isMapView = false
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupMap()
        observeViewModel()

        viewModel.fetchTrips()

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchDestination.text.toString().trim()
            viewModel.filterTrips(query)
        }

        binding.btnToggleView.setOnClickListener {
            toggleView()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(
            trips = emptyList(),
            onJoinClick = { trip -> joinTrip(trip) },
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

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun observeViewModel() {
        viewModel.trips.observe(this) { trips ->
            adapter.updateList(trips)
            updateMapMarkers(trips)
            
            if (trips.isEmpty()) {
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.rvTrips.visibility = View.GONE
            } else {
                binding.emptyStateContainer.visibility = View.GONE
                if (!isMapView) binding.rvTrips.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        viewModel.trips.value?.let { updateMapMarkers(it) }
    }

    private fun toggleView() {
        isMapView = !isMapView
        if (isMapView) {
            binding.rvTrips.visibility = View.GONE
            findViewById<View>(R.id.mapFragment).visibility = View.VISIBLE
            binding.btnToggleView.text = "Show List"
            binding.btnToggleView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_agenda, 0, 0, 0)
            viewModel.trips.value?.let { updateMapMarkers(it) }
        } else {
            binding.rvTrips.visibility = View.VISIBLE
            findViewById<View>(R.id.mapFragment).visibility = View.GONE
            binding.btnToggleView.text = "Show Map"
            binding.btnToggleView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_dialog_map, 0, 0, 0)
        }
    }

    private fun updateMapMarkers(trips: List<Trip>) {
        val map = googleMap ?: return
        map.clear()

        if (trips.isEmpty()) return

        val builder = LatLngBounds.Builder()
        var hasPoints = false

        trips.forEach { trip ->
            val latLng = LatLng(trip.pickupLat, trip.pickupLng)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("To: ${trip.destination}")
                    .snippet("₹${trip.fare} | ${trip.availableSeats} seats left")
            )?.tag = trip

            builder.include(latLng)
            hasPoints = true
        }

        map.setOnInfoWindowClickListener { marker ->
            val trip = marker.tag as? Trip
            trip?.let {
                val intent = Intent(this, TripDetailActivity::class.java)
                intent.putExtra("tripId", it.tripId)
                startActivity(intent)
            }
        }

        if (hasPoints) {
            try {
                val bounds = builder.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            } catch (e: Exception) {
                // Handle case where bounds cannot be built
            }
        }
    }

    private fun joinTrip(trip: Trip) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        binding.progressBar.visibility = View.VISIBLE
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
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Joined successfully! Check 'My Bookings'", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { e ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
