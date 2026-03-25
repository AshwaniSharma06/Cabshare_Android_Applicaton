package com.example.cabshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.MatchAdapter
import com.example.cabshare.databinding.ActivityMatchesBinding
import com.example.cabshare.viewmodel.MatchesViewModel
import com.google.firebase.auth.FirebaseAuth

class MatchesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatchesBinding
    private val viewModel: MatchesViewModel by viewModels()
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        viewModel.fetchMatches()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = MatchAdapter(mutableListOf()) { match ->
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isUser1 = match.userId1 == currentUserId
            
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("receiverId", if (isUser1) match.userId2 else match.userId1)
                putExtra("receiverName", if (isUser1) match.userName2 else match.userName1)
            }
            startActivity(intent)
        }
        binding.rvMatches.layoutManager = LinearLayoutManager(this)
        binding.rvMatches.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.matches.observe(this) { matches ->
            if (matches.isEmpty()) {
                binding.tvNoMatches.visibility = View.VISIBLE
                binding.rvMatches.visibility = View.GONE
            } else {
                binding.tvNoMatches.visibility = View.GONE
                binding.rvMatches.visibility = View.VISIBLE
                adapter.updateMatches(matches)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
