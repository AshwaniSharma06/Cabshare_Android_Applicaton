package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.SupportTicket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HelpViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _ticketSubmitted = MutableLiveData<Boolean>()
    val ticketSubmitted: LiveData<Boolean> = _ticketSubmitted

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun submitTicket(issueType: String, description: String) {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email ?: ""

        if (issueType.isEmpty() || issueType == "Issue Type") {
            _error.value = "Please select an issue type"
            return
        }

        if (description.trim().isEmpty()) {
            _error.value = "Please describe your issue"
            return
        }

        _isLoading.value = true
        val ticketId = db.collection("supportTickets").document().id
        val ticket = SupportTicket(
            ticketId = ticketId,
            userId = userId,
            userEmail = userEmail,
            issueType = issueType,
            description = description,
            status = "open",
            createdAt = System.currentTimeMillis()
        )

        db.collection("supportTickets").document(ticketId).set(ticket)
            .addOnSuccessListener {
                _isLoading.value = false
                _ticketSubmitted.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
            }
    }

    fun clearStatus() {
        _ticketSubmitted.value = false
        _error.value = null
    }
}
