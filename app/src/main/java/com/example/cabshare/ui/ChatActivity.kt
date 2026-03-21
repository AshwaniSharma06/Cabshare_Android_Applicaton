package com.example.cabshare.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.ChatAdapter
import com.example.cabshare.databinding.ActivityChatBinding
import com.example.cabshare.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ChatAdapter
    private var messages = mutableListOf<Message>()
    private var receiverId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        receiverId = intent.getStringExtra("receiverId")
        val receiverName = intent.getStringExtra("receiverName")

        supportActionBar?.title = receiverName ?: "Chat"

        setupRecyclerView()
        listenForMessages()

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty() && receiverId != null) {
                sendMessage(text)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter
    }

    private fun sendMessage(text: String) {
        val senderId = auth.currentUser?.uid ?: return
        val message = Message(senderId, receiverId!!, text)
        
        db.collection("messages").add(message)
            .addOnSuccessListener {
                binding.etMessage.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForMessages() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    messages.clear()
                    for (doc in snapshot.documents) {
                        val msg = doc.toObject(Message::class.java)
                        if (msg != null && 
                            ((msg.senderId == currentUserId && msg.receiverId == receiverId) || 
                             (msg.senderId == receiverId && msg.receiverId == currentUserId))) {
                            messages.add(msg)
                        }
                    }
                    adapter.updateMessages(messages)
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
    }
}
