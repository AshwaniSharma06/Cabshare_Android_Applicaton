package com.example.cabshare.model

data class Review(
    val reviewId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val rating: Float = 0f,
    val feedback: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
