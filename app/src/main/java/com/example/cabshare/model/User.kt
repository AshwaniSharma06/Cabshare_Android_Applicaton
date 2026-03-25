package com.example.cabshare.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val rating: Float = 5.0f,
    val profileImageUrl: String? = null,
    val bio: String = "",
    val fcmToken: String = "",
    val totalTrips: Int = 0,
    val memberSince: Timestamp? = null,
    val totalReviews: Int = 0,
    val isVerified: Boolean = false
)
