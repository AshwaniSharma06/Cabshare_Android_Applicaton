package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchNotifications() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("notifications")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                _isLoading.value = false
                if (e != null) return@addSnapshotListener

                val list = snapshots?.toObjects(Notification::class.java) ?: emptyList()
                _notifications.value = list
                _unreadCount.value = list.count { !it.isRead }
            }
    }

    fun markAsRead(notificationId: String) {
        db.collection("notifications").document(notificationId)
            .update("isRead", true)
    }

    fun markAllAsRead() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("notifications")
            .whereEqualTo("userId", uid)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()
                for (doc in snapshots) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit()
            }
    }
}
