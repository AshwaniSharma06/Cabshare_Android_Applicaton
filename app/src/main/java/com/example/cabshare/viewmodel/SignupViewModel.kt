package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _signupSuccess = MutableLiveData<Boolean>()
    val signupSuccess: LiveData<Boolean> = _signupSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun signup(name: String, email: String, password: String) {
        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val user = User(uid, name, email)
                    saveUserToFirestore(user)
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message ?: "Signup failed"
                }
            }
    }

    private fun saveUserToFirestore(user: User) {
        db.collection("users").document(user.uid).set(user)
            .addOnSuccessListener {
                _isLoading.value = false
                _signupSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "Failed to save profile: ${e.message}"
            }
    }

    fun clearError() {
        _error.value = null
    }
}
