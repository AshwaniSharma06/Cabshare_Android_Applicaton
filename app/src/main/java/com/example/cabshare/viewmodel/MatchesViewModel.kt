package com.example.cabshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.Match
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MatchesViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _matches = MutableLiveData<List<Match>>()
    val matches: LiveData<List<Match>> = _matches

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchMatches() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        // Query matches where the current user is either user1 or user2
        db.collection("matches")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                _isLoading.value = false
                if (e != null) return@addSnapshotListener

                val matchList = mutableListOf<Match>()
                snapshots?.forEach { doc ->
                    val match = doc.toObject(Match::class.java)
                    if (match.userId1 == userId || match.userId2 == userId) {
                        matchList.add(match)
                    }
                }
                _matches.value = matchList
            }
    }
}
