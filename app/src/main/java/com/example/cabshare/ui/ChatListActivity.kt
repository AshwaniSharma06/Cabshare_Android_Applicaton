package com.example.cabshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.ChatListAdapter
import com.example.cabshare.databinding.ActivityChatListBinding
import com.example.cabshare.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadChats()

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter { chat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chat.chatId)
            // Use "receiverId" to match ChatActivity's expectation
            intent.putExtra("receiverId", chat.users.first { it != auth.currentUser?.uid })
            intent.putExtra("receiverName", chat.otherUserName)
            startActivity(intent)
        }
        binding.rvChatList.layoutManager = LinearLayoutManager(this)
        binding.rvChatList.adapter = adapter
    }

    private fun loadChats() {
        val currentUserId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        db.collection("chats")
            .whereArrayContains("users", currentUserId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                binding.progressBar.visibility = View.GONE
                if (e != null) {
                    // This error usually indicates a missing index.
                    return@addSnapshotListener
                }

                val chats = mutableListOf<Chat>()
                snapshots?.forEach { doc ->
                    val chat = doc.toObject(Chat::class.java).copy(chatId = doc.id)
                    
                    // Fetch other user info
                    val otherUserId = chat.users.firstOrNull { it != currentUserId }
                    if (otherUserId != null) {
                        db.collection("users").document(otherUserId).get()
                            .addOnSuccessListener { userDoc ->
                                chat.otherUserName = userDoc.getString("name") ?: "Unknown"
                                chat.otherUserProfileImage = userDoc.getString("profileImageUrl") ?: ""
                                adapter.notifyDataSetChanged()
                            }
                    }
                    chats.add(chat)
                }
                
                adapter.submitList(chats)
                binding.tvEmptyState.visibility = if (chats.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}
