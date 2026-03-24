package com.example.cabshare.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val messageId: String = "",
    val chatId: String = "", // Combined IDs for efficient filtering
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val seen: Boolean = false
)
