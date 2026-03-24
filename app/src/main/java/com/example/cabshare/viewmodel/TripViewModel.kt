package com.example.cabshare.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Trip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class TripViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var allTrips = listOf<Trip>()

    fun fetchTrips() {
        _isLoading.value = true
        db.collection("trips")
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    allTrips = snapshot.toObjects(Trip::class.java)
                    _trips.value = allTrips
                }
            }
    }

    fun filterTrips(
        query: String,
        targetTime: String? = null,
        userLat: Double? = null,
        userLng: Double? = null,
        radiusKm: Double = 5.0,
        timeFlexMinutes: Int = 30
    ) {
        val filtered = allTrips.filter { trip ->
            // 1. Partial Destination/Starting Match
            val matchText = query.isEmpty() || 
                    trip.destination.contains(query, ignoreCase = true) ||
                    trip.startingLocation.contains(query, ignoreCase = true)

            // 2. Nearby Pickup Radius
            val matchLocation = if (userLat != null && userLng != null) {
                calculateDistance(userLat, userLng, trip.pickupLat, trip.pickupLng) <= radiusKm
            } else {
                true
            }

            // 3. Time Flexibility
            val matchTime = if (!targetTime.isNullOrEmpty()) {
                isTimeWithinRange(trip.time, targetTime, timeFlexMinutes)
            } else {
                true
            }
            
            matchText && matchLocation && matchTime
        }
        _trips.value = filtered
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000 // Convert to KM
    }

    private fun isTimeWithinRange(tripTime: String, targetTime: String, flexMinutes: Int): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date1 = sdf.parse(tripTime)
            val date2 = sdf.parse(targetTime)
            if (date1 == null || date2 == null) return true
            
            val diff = Math.abs(date1.time - date2.time)
            val diffMinutes = diff / (60 * 1000)
            diffMinutes <= flexMinutes
        } catch (e: Exception) {
            true
        }
    }
}
