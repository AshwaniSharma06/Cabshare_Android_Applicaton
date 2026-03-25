package com.example.cabshare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.cabshare.databinding.ActivityMainBinding
import com.example.cabshare.ui.*
import com.example.cabshare.viewmodel.MainViewModel
import com.example.cabshare.viewmodel.NotificationViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askNotificationPermission()
        setupObservers()
        setupListeners()
        
        viewModel.updateFcmToken()
        notificationViewModel.fetchNotifications()
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            user?.let {
                binding.tvHomeName.text = it.name
                
                // Glide with cache-busting to ensure fresh profile image
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .into(binding.ivHomeProfile)
            }
        }

        notificationViewModel.unreadCount.observe(this) { count ->
            binding.notificationBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        binding.btnCreateTrip.setOnClickListener {
            startActivity(Intent(this, CreateTripActivity::class.java))
        }

        binding.btnFindTrip.setOnClickListener {
            startActivity(Intent(this, FindTripActivity::class.java))
        }

        binding.btnMyTrips.setOnClickListener {
            startActivity(Intent(this, MyTripsActivity::class.java))
        }

        binding.btnChats.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        binding.btnMatches.setOnClickListener {
            startActivity(Intent(this, MatchesActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.ivHomeProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile()
        notificationViewModel.fetchNotifications()
    }
}
