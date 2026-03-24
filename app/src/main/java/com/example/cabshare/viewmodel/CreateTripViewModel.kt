package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Trip
import com.example.cabshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateTripViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _tripPosted = MutableLiveData<Boolean>()
    val tripPosted: LiveData<Boolean> = _tripPosted

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchCurrentUser() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _currentUser.value = doc.toObject(User::class.java)
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to fetch user data: ${e.message}"
            }
    }

    fun submitTrip(
        startingLoc: String,
        destination: String,
        pickupLat: Double,
        pickupLng: Double,
        destLat: Double,
        destLng: Double,
        date: String,
        time: String,
        fare: String,
        seatsStr: String
    ) {
        val seats = seatsStr.toIntOrNull()
        if (seats == null) {
            _error.value = "Invalid number of seats"
            return
        }

        val user = _currentUser.value
        val userId = auth.currentUser?.uid ?: ""
        val userName = user?.name ?: "User"
        val userRating = user?.rating ?: 5.0f
        val userProfileImage = user?.profileImageUrl

        _isLoading.value = true
        val tripId = db.collection("trips").document().id
        val trip = Trip(
            tripId = tripId,
            userId = userId,
            userName = userName,
            userRating = userRating,
            userProfileImage = userProfileImage,
            startingLocation = startingLoc,
            destination = destination,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            destLat = destLat,
            destLng = destLng,
            date = date,
            time = time,
            fare = fare,
            totalSeats = seats,
            availableSeats = seats,
            timestamp = System.currentTimeMillis()
        )

        db.collection("trips").document(tripId).set(trip)
            .addOnSuccessListener {
                _isLoading.value = false
                _tripPosted.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "Error posting trip: ${e.message}"
            }
    }

    fun clearError() {
        _error.value = null
    }
}
