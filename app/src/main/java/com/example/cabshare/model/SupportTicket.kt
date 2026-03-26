package com.example.cabshare.model

data class SupportTicket(
    val ticketId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val issueType: String = "",
    val description: String = "",
    val status: String = "open", // open, resolved
    val createdAt: Long = System.currentTimeMillis(),
    val screenshotUrl: String? = null
)
