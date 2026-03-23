package com.example.cabshare.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cabshare.databinding.ActivityTripDetailBinding
import com.example.cabshare.model.Trip
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore

class TripDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityTripDetailBinding
    private lateinit var mMap: GoogleMap
    private var tripId: String? = null
    private var trip: Trip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getStringExtra("tripId")
        
        val mapFragment = supportFragmentManager
            .findFragmentById(binding.mapDetail.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fetchTripDetails()

        binding.btnDetailChat.setOnClickListener {
            trip?.let {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiverId", it.userId)
                intent.putExtra("receiverName", it.userName)
                startActivity(intent)
            }
        }
    }

    private fun fetchTripDetails() {
        val id = tripId ?: return
        FirebaseFirestore.getInstance().collection("trips").document(id)
            .get()
            .addOnSuccessListener { document ->
                trip = document.toObject(Trip::class.java)
                trip?.let { updateUI(it) }
            }
    }

    private fun updateUI(trip: Trip) {
        binding.tvDetailUser.text = "Posted by: ${trip.userName}"
        binding.tvDetailPickup.text = "From: ${trip.startingLocation}"
        binding.tvDetailDestination.text = "To: ${trip.destination}"
        binding.tvDetailDatetime.text = "${trip.date} at ${trip.time}"
        binding.tvDetailFare.text = "Fare: ₹${trip.fare}"

        if (::mMap.isInitialized) {
            drawRouteOnMap(trip)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        trip?.let { drawRouteOnMap(it) }
    }

    private fun drawRouteOnMap(trip: Trip) {
        val pickup = LatLng(trip.pickupLat, trip.pickupLng)
        val dest = LatLng(trip.destLat, trip.destLng)

        mMap.clear()
        
        // Add markers
        mMap.addMarker(MarkerOptions().position(pickup).title("Pickup: ${trip.startingLocation}"))
        mMap.addMarker(MarkerOptions().position(dest).title("Destination: ${trip.destination}"))

        // Draw a straight line (Polyline)
        mMap.addPolyline(
            PolylineOptions()
                .add(pickup, dest)
                .width(10f)
                .color(Color.BLUE)
                .geodesic(true)
        )

        // Adjust camera to show both points
        val bounds = LatLngBounds.Builder()
            .include(pickup)
            .include(dest)
            .build()
        
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
    }
}
