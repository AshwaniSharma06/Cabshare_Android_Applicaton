package com.example.cabshare.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val rating: Float = 5.0f,
    val profileImageUrl: String? = null,
    val bio: String = ""
)
