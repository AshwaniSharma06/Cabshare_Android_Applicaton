package com.example.cabshare.model

data class Trip(
    val tripId: String = "",
    val userId: String = "",
    val userName: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val fare: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
