package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Trip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TripDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
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
        val nextStatus = if (currentStatus == "pending") "started" else "completed"
        
        db.collection("trips").document(tripId).update("status", nextStatus)
            .addOnSuccessListener {
                _statusUpdateSuccess.value = if (nextStatus == "started") "Trip Started!" else "Trip Completed!"
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to update status: ${e.message}"
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
