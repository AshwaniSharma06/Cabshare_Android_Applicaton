package com.example.cabshare.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.adapter.NotificationAdapter
import com.example.cabshare.databinding.ActivityNotificationBinding
import com.example.cabshare.viewmodel.NotificationViewModel

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        viewModel.fetchNotifications()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            viewModel.markAsRead(notification.notificationId)
            // Navigate based on notification type if needed
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.notifications.observe(this) { notifications ->
            if (notifications.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvNotifications.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.rvNotifications.visibility = View.VISIBLE
                adapter.setNotifications(notifications)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
