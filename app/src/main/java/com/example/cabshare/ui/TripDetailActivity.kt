package com.example.cabshare.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.cabshare.R
import com.example.cabshare.databinding.ActivityTripDetailBinding
import com.example.cabshare.model.Trip
import com.example.cabshare.viewmodel.TripDetailViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth

class TripDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityTripDetailBinding
    private val viewModel: TripDetailViewModel by viewModels()
    private var mMap: GoogleMap? = null
    private var tripId: String? = null
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getStringExtra("tripId")
        
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_detail) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupObservers()
        
        tripId?.let { viewModel.fetchTripDetails(it) }

        binding.btnDetailChat.setOnClickListener {
            viewModel.trip.value?.let {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiverId", it.userId)
                intent.putExtra("receiverName", it.userName)
                startActivity(intent)
            }
        }

        binding.btnJoinTrip.setOnClickListener {
            tripId?.let { viewModel.joinTrip(it) }
        }

        binding.btnLeaveTrip.setOnClickListener {
            tripId?.let { viewModel.leaveTrip(it) }
        }

        binding.btnCompleteTrip.setOnClickListener {
            val trip = viewModel.trip.value
            if (trip != null && tripId != null) {
                viewModel.updateTripStatus(tripId!!, trip.status)
            }
        }
        
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.trip.observe(this) { trip ->
            trip?.let { updateUI(it) }
        }

        viewModel.statusUpdateSuccess.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearStatusUpdate()
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateUI(trip: Trip) {
        val currentUserId = auth.currentUser?.uid
        
        binding.tvDetailUser.text = trip.userName
        binding.tvDetailDriverRating.text = "⭐ ${String.format("%.1f", trip.userRating)}"
        binding.tvDetailPickup.text = trip.startingLocation
        binding.tvDetailDestination.text = trip.destination
        binding.tvDetailFare.text = "₹${trip.fare}"

        // New Preferences
        binding.chipDetailAC.visibility = if (trip.isAC) View.VISIBLE else View.GONE
        binding.tvGenderPref.text = "Gender Preference: ${trip.genderPreference}"

        // Fare Split Calculation
        val fareInt = trip.fare.toIntOrNull() ?: 0
        val passengerCount = trip.passengers.size + 1 // +1 for driver
        val splitFare = if (passengerCount > 0) fareInt / passengerCount else fareInt
        binding.tvFareSplit.text = "₹$splitFare"

        // Driver Image with cache-busting
        Glide.with(this)
            .load(trip.userProfileImage)
            .placeholder(R.drawable.ic_default_profile)
            .error(R.drawable.ic_default_profile)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .circleCrop()
            .into(binding.ivDetailDriverImage)

        // Map Route update
        mMap?.let { drawRouteOnMap(it, trip) }

        // Logic for buttons (Driver vs Passenger)
        if (trip.userId == currentUserId) {
            // DRIVER LOGIC
            binding.btnJoinTrip.visibility = View.GONE
            binding.btnLeaveTrip.visibility = View.GONE
            binding.btnDetailChat.visibility = View.GONE
            
            when (trip.status) {
                "pending" -> {
                    binding.btnCompleteTrip.visibility = View.VISIBLE
                    binding.btnCompleteTrip.text = "Start Trip"
                }
                "started" -> {
                    binding.btnCompleteTrip.visibility = View.VISIBLE
                    binding.btnCompleteTrip.text = "Complete Trip"
                }
                else -> {
                    binding.btnCompleteTrip.visibility = View.GONE
                }
            }
        } else {
            // PASSENGER LOGIC
            binding.btnCompleteTrip.visibility = View.GONE
            
            if (trip.status == "completed") {
                binding.btnJoinTrip.visibility = View.GONE
                binding.btnLeaveTrip.visibility = View.GONE
                binding.btnDetailChat.visibility = View.GONE
                if (trip.passengers.contains(currentUserId)) {
                    showRatingDialog()
                }
            } else {
                if (trip.passengers.contains(currentUserId)) {
                    // Joined
                    binding.btnJoinTrip.visibility = View.GONE
                    binding.btnLeaveTrip.visibility = View.VISIBLE
                    binding.btnDetailChat.visibility = View.VISIBLE
                } else {
                    // Not joined
                    binding.btnLeaveTrip.visibility = View.GONE
                    binding.btnDetailChat.visibility = View.GONE
                    if (trip.availableSeats > 0 && trip.status == "pending") {
                        binding.btnJoinTrip.visibility = View.VISIBLE
                        binding.btnJoinTrip.text = "Join Trip (${trip.availableSeats} seats left)"
                    } else {
                        binding.btnJoinTrip.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showRatingDialog() {
        viewModel.trip.value?.let {
            val dialog = RatingDialog(this, it.userId, it.tripId) {
                // Callback
            }
            dialog.show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.uiSettings?.isZoomControlsEnabled = false
        mMap?.uiSettings?.isMapToolbarEnabled = false
        viewModel.trip.value?.let { drawRouteOnMap(googleMap, it) }
    }

    private fun drawRouteOnMap(map: GoogleMap, trip: Trip) {
        if (trip.pickupLat == 0.0 || trip.destLat == 0.0) return

        val pickup = LatLng(trip.pickupLat, trip.pickupLng)
        val dest = LatLng(trip.destLat, trip.destLng)

        map.clear()
        
        // Custom Blue Marker for Pickup
        map.addMarker(MarkerOptions()
            .position(pickup)
            .title("Pickup: ${trip.startingLocation}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        // Custom Red Marker for Destination
        map.addMarker(MarkerOptions()
            .position(dest)
            .title("Drop-off: ${trip.destination}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

        // Sophisticated Polyline
        map.addPolyline(
            PolylineOptions()
                .add(pickup, dest)
                .width(14f)
                .color(Color.parseColor("#4A90E2"))
                .geodesic(true)
        )

        try {
            val bounds = LatLngBounds.Builder()
                .include(pickup)
                .include(dest)
                .build()
            
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250))
        } catch (e: Exception) {}
    }
}
