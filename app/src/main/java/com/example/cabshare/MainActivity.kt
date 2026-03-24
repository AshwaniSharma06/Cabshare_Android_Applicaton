package com.example.cabshare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.cabshare.auth.LoginActivity
import com.example.cabshare.databinding.ActivityMainBinding
import com.example.cabshare.ui.CreateTripActivity
import com.example.cabshare.ui.FindTripActivity
import com.example.cabshare.ui.MyTripsActivity
import com.example.cabshare.ui.ProfileActivity
import com.example.cabshare.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

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
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            user?.let {
                binding.tvHomeName.text = it.name
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .circleCrop()
                    .into(binding.ivHomeProfile)
            }
        }
    }

    private fun setupListeners() {
        binding.btnCreateTrip.setOnClickListener {
            startActivity(Intent(this, CreateTripActivity::class.java))
        }

        binding.btnFindTrip.setOnClickListener {
            startActivity(Intent(this, FindTripActivity::class.java))
        }

        binding.btnMyTrips.setOnClickListener {
            startActivity(Intent(this, MyTripsActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.ivHomeProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            viewModel.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
    }
}
