package com.example.cabshare.model

data class Notification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // e.g., "trip_posted", "match_found", "trip_started"
    val relatedId: String = "", // e.g., tripId or matchId
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false
)
