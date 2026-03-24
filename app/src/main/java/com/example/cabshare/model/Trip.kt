package com.example.cabshare.model

data class Trip(
    val tripId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRating: Float = 5.0f,
    val userProfileImage: String? = null,
    val startingLocation: String = "",
    val destination: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val destLat: Double = 0.0,
    val destLng: Double = 0.0,
    val date: String = "",
    val time: String = "",
    val fare: String = "",
    val totalSeats: Int = 4,
    val availableSeats: Int = 4,
    val passengers: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending", // "pending", "started", "completed", "cancelled"
    val distance: String = "",
    val duration: String = ""
)
