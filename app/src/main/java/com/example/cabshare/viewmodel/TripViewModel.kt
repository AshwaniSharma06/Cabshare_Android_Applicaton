package com.example.cabshare.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.math.*

class TripViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allTrips = listOf<Trip>()
    
    private var currentFilterQuery = ""
    private var currentFilterAC = false
    private var currentFilterGender = "Any"
    private var searchLat: Double? = null
    private var searchLng: Double? = null
    private val SEARCH_RADIUS_KM = 25.0

    fun fetchTrips() {
        _isLoading.value = true
        val currentUserId = auth.currentUser?.uid
        
        db.collection("trips")
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    Log.e("TripViewModel", "Error fetching trips: ${e.message}")
                    _error.value = "Error: ${e.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    allTrips = snapshot.toObjects(Trip::class.java).filter { 
                        // Hide your own trips in "Find Rides" and only show trips with seats
                        it.userId != currentUserId && it.availableSeats > 0 
                    }
                    applyAllFilters()
                }
            }
    }

    fun setQueryFilter(query: String, lat: Double? = null, lng: Double? = null) {
        currentFilterQuery = query
        searchLat = lat
        searchLng = lng
        applyAllFilters()
    }

    fun setACFilter(isAC: Boolean) {
        currentFilterAC = isAC
        applyAllFilters()
    }

    fun setGenderFilter(gender: String) {
        currentFilterGender = gender
        applyAllFilters()
    }

    private fun applyAllFilters() {
        val filtered = allTrips.filter { trip ->
            // 1. Text Search OR Proximity Search
            val matchText = currentFilterQuery.isEmpty() || 
                    trip.destination.contains(currentFilterQuery, ignoreCase = true) ||
                    trip.startingLocation.contains(currentFilterQuery, ignoreCase = true)

            val matchProximity = if (searchLat != null && searchLng != null) {
                calculateDistance(searchLat!!, searchLng!!, trip.pickupLat, trip.pickupLng) <= SEARCH_RADIUS_KM ||
                calculateDistance(searchLat!!, searchLng!!, trip.destLat, trip.destLng) <= SEARCH_RADIUS_KM
            } else {
                false
            }

            val finalLocationMatch = if (currentFilterQuery.isNotEmpty()) (matchText || matchProximity) else true

            // 2. AC Filter
            val matchAC = !currentFilterAC || trip.isAC

            // 3. Gender Filter
            val matchGender = when (currentFilterGender) {
                "Male only" -> trip.genderPreference == "Male only"
                "Female only" -> trip.genderPreference == "Female only"
                else -> true
            }
            
            finalLocationMatch && matchAC && matchGender
        }
        _trips.value = filtered
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun clearError() {
        _error.value = null
    }
}
