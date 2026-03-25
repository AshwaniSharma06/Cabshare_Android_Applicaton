package com.example.cabshare.model

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",
    val users: List<String> = emptyList(),
    val tripId: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Timestamp? = null,
    // Transient fields for UI
    var otherUserName: String = "",
    var otherUserProfileImage: String = ""
)
