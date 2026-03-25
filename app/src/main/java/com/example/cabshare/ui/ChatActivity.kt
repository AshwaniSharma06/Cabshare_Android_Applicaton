package com.example.cabshare.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.ChatAdapter
import com.example.cabshare.databinding.ActivityChatBinding
import com.example.cabshare.viewmodel.ChatViewModel

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val receiverId = intent.getStringExtra("receiverId")
        val receiverName = intent.getStringExtra("receiverName")

        if (receiverId == null) {
            finish()
            return
        }

        binding.tvReceiverName.text = receiverName ?: "Chat"

        viewModel.initChat(receiverId)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(mutableListOf())
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            adapter.updateMessages(messages)
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.typingStatus.observe(this) { isTyping ->
            binding.tvTypingStatus.visibility = if (isTyping) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.etMessage.text.clear()
                viewModel.updateTypingStatus(false)
            }
        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateTypingStatus(s?.isNotEmpty() == true)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
