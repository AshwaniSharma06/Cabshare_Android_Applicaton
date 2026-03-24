package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _typingStatus = MutableLiveData<Boolean>()
    val typingStatus: LiveData<Boolean> = _typingStatus

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var chatId: String = ""
    private var receiverId: String = ""

    fun initChat(receiverId: String) {
        this.receiverId = receiverId
        val currentUserId = auth.currentUser?.uid ?: return
        chatId = if (currentUserId < receiverId) "${currentUserId}_${receiverId}" else "${receiverId}_$currentUserId"
        listenForMessages()
        listenForTypingStatus()
    }

    private fun listenForMessages() {
        _isLoading.value = true
        db.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val msgList = snapshot.toObjects(Message::class.java)
                    _messages.value = msgList
                    
                    // Mark as seen logic (could be done here or in Activity)
                    val currentUserId = auth.currentUser?.uid
                    msgList.forEach { msg ->
                        if (msg.receiverId == currentUserId && !msg.seen) {
                            markMessageAsSeen(msg.messageId)
                        }
                    }
                }
            }
    }

    private fun markMessageAsSeen(messageId: String) {
        db.collection("messages").document(messageId).update("seen", true)
    }

    fun sendMessage(text: String) {
        val senderId = auth.currentUser?.uid ?: return
        val messageRef = db.collection("messages").document()
        val message = Message(
            messageId = messageRef.id,
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            message = text,
            timestamp = null, // Firestore server timestamp preferred but using local or null handled by Firestore
            seen = false
        )

        messageRef.set(message).addOnFailureListener { e ->
            _error.value = "Failed to send: ${e.message}"
        }
    }

    private fun listenForTypingStatus() {
        db.collection("typing_status").document(receiverId)
            .addSnapshotListener { snapshot, _ ->
                _typingStatus.value = snapshot?.getBoolean("isTyping") ?: false
            }
    }

    fun updateTypingStatus(isTyping: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("typing_status").document(currentUserId)
            .set(mapOf("isTyping" to isTyping))
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        updateTypingStatus(false)
    }
}
