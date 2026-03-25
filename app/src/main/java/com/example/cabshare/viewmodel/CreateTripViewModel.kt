package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Match
import com.example.cabshare.model.Notification
import com.example.cabshare.model.Trip
import com.example.cabshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.*

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
        seatsStr: String,
        isAC: Boolean,
        genderPreference: String
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
            timestamp = System.currentTimeMillis(),
            isAC = isAC,
            genderPreference = genderPreference
        )

        db.collection("trips").document(tripId).set(trip)
            .addOnSuccessListener {
                sendNotification(
                    userId,
                    "Ride Posted",
                    "Your trip to $destination has been successfully posted!",
                    "trip_posted",
                    tripId
                )
                checkForMatches(trip)
                _isLoading.value = false
                _tripPosted.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "Error posting trip: ${e.message}"
            }
    }

    private fun checkForMatches(newTrip: Trip) {
        db.collection("trips")
            .whereEqualTo("date", newTrip.date)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots) {
                    val otherTrip = doc.toObject(Trip::class.java)
                    if (otherTrip.userId == newTrip.userId) continue

                    val distance = calculateDistance(
                        newTrip.pickupLat, newTrip.pickupLng,
                        otherTrip.pickupLat, otherTrip.pickupLng
                    )

                    if (distance <= 10.0) {
                        createMatch(newTrip, otherTrip)
                    }
                }
            }
    }

    private fun createMatch(trip1: Trip, trip2: Trip) {
        val matchId = if (trip1.tripId < trip2.tripId) "${trip1.tripId}_${trip2.tripId}" else "${trip2.tripId}_${trip1.tripId}"
        
        val match = Match(
            matchId = matchId,
            tripId1 = trip1.tripId,
            tripId2 = trip2.tripId,
            userId1 = trip1.userId,
            userId2 = trip2.userId,
            userName1 = trip1.userName,
            userName2 = trip2.userName,
            userImage1 = trip1.userProfileImage,
            userImage2 = trip2.userProfileImage,
            location1 = trip1.startingLocation,
            location2 = trip2.startingLocation,
            date = trip1.date,
            timestamp = System.currentTimeMillis()
        )

        db.collection("matches").document(matchId).set(match)
            .addOnSuccessListener {
                // Notify both users
                sendNotification(
                    trip1.userId,
                    "Match Found!",
                    "We found a nearby ride matching your trip to ${trip1.destination}!",
                    "match_found",
                    matchId
                )
                sendNotification(
                    trip2.userId,
                    "Match Found!",
                    "We found a nearby ride matching your trip to ${trip2.destination}!",
                    "match_found",
                    matchId
                )
            }
    }

    private fun sendNotification(userId: String, title: String, message: String, type: String, relatedId: String) {
        val notificationId = db.collection("notifications").document().id
        val notification = Notification(
            notificationId = notificationId,
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedId = relatedId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        db.collection("notifications").document(notificationId).set(notification)
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
