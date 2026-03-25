package com.example.cabshare.model

data class Match(
    val matchId: String = "",
    val tripId1: String = "",
    val tripId2: String = "",
    val userId1: String = "",
    val userId2: String = "",
    val userName1: String = "",
    val userName2: String = "",
    val userImage1: String? = null,
    val userImage2: String? = null,
    val location1: String = "",
    val location2: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
