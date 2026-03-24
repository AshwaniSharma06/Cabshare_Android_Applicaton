package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyTripsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    fun fetchTrips(isOffering: Boolean, isHistoryMode: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val currentTime = System.currentTimeMillis()
        val twentyFourHoursAgo = currentTime - (24 * 60 * 60 * 1000)

        _isLoading.value = true
        
        val baseQuery = db.collection("trips")
        val filterQuery = if (isOffering) {
            baseQuery.whereEqualTo("userId", uid)
        } else {
            baseQuery.whereArrayContains("passengers", uid)
        }

        val finalQuery = if (isHistoryMode) {
            filterQuery.whereLessThan("timestamp", twentyFourHoursAgo)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            filterQuery.whereGreaterThanOrEqualTo("timestamp", twentyFourHoursAgo)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        }

        finalQuery.addSnapshotListener { value, e ->
            _isLoading.value = false
            if (e != null) {
                _error.value = e.message
                return@addSnapshotListener
            }

            val tripList = value?.toObjects(Trip::class.java) ?: emptyList()
            _trips.value = tripList
        }
    }

    fun deleteTrip(tripId: String) {
        _isLoading.value = true
        db.collection("trips").document(tripId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                _deleteSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
            }
    }

    fun clearError() {
        _error.value = null
    }
    
    fun resetDeleteSuccess() {
        _deleteSuccess.value = false
    }
}
