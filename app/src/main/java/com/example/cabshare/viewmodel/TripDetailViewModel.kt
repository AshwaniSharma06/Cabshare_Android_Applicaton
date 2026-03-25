package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TripDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _trip = MutableLiveData<Trip?>()
    val trip: LiveData<Trip?> = _trip

    private val _statusUpdateSuccess = MutableLiveData<String?>()
    val statusUpdateSuccess: LiveData<String?> = _statusUpdateSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var tripListener: ListenerRegistration? = null

    fun fetchTripDetails(tripId: String) {
        tripListener?.remove()
        tripListener = db.collection("trips").document(tripId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _trip.value = snapshot.toObject(Trip::class.java)
                }
            }
    }

    fun updateTripStatus(tripId: String, currentStatus: String) {
        val nextStatus = when (currentStatus) {
            "pending" -> "started"
            "started" -> "completed"
            else -> return
        }
        
        db.collection("trips").document(tripId).update("status", nextStatus)
            .addOnSuccessListener {
                _statusUpdateSuccess.value = if (nextStatus == "started") "Trip Started!" else "Trip Completed!"
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to update status: ${e.message}"
            }
    }

    fun joinTrip(tripId: String) {
        val userId = auth.currentUser?.uid ?: return
        val tripDoc = db.collection("trips").document(tripId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(tripDoc)
            val trip = snapshot.toObject(Trip::class.java) ?: return@runTransaction

            if (trip.availableSeats > 0 && !trip.passengers.contains(userId)) {
                transaction.update(tripDoc, "passengers", FieldValue.arrayUnion(userId))
                transaction.update(tripDoc, "availableSeats", trip.availableSeats - 1)
            } else {
                throw Exception("No seats available or already joined")
            }
        }.addOnSuccessListener {
            _statusUpdateSuccess.value = "Successfully joined the trip!"
        }.addOnFailureListener { e ->
            _error.value = e.message
        }
    }

    fun leaveTrip(tripId: String) {
        val userId = auth.currentUser?.uid ?: return
        val tripDoc = db.collection("trips").document(tripId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(tripDoc)
            val trip = snapshot.toObject(Trip::class.java) ?: return@runTransaction

            if (trip.passengers.contains(userId)) {
                transaction.update(tripDoc, "passengers", FieldValue.arrayRemove(userId))
                transaction.update(tripDoc, "availableSeats", trip.availableSeats + 1)
            }
        }.addOnSuccessListener {
            _statusUpdateSuccess.value = "You left the trip."
        }.addOnFailureListener { e ->
            _error.value = e.message
        }
    }

    fun clearStatusUpdate() {
        _statusUpdateSuccess.value = null
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        tripListener?.remove()
    }
}
