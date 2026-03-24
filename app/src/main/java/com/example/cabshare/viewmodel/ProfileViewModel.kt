package com.example.cabshare.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _isLoading.value = false
                if (document != null && document.exists()) {
                    _user.value = document.toObject(User::class.java)
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _statusMessage.value = "Failed to load profile: ${e.message}"
            }
    }

    fun saveProfile(bio: String, selectedImageUri: Uri?) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        if (selectedImageUri != null) {
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(selectedImageUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        updateUserInFirestore(uid, bio, url.toString())
                    }
                }
                .addOnFailureListener {
                    _isLoading.value = false
                    _statusMessage.value = "Image upload failed"
                }
        } else {
            updateUserInFirestore(uid, bio, null)
        }
    }

    private fun updateUserInFirestore(uid: String, bio: String, imageUrl: String?) {
        val updates = mutableMapOf<String, Any>("bio" to bio)
        if (imageUrl != null) {
            updates["profileImageUrl"] = imageUrl
        }

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                _statusMessage.value = "Profile updated successfully!"
                fetchUserProfile() // Refresh local data
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _statusMessage.value = "Update failed: ${e.message}"
            }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
