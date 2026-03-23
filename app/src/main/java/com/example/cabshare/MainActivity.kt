package com.example.cabshare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.cabshare.auth.LoginActivity
import com.example.cabshare.databinding.ActivityMainBinding
import com.example.cabshare.model.User
import com.example.cabshare.ui.CreateTripActivity
import com.example.cabshare.ui.FindTripActivity
import com.example.cabshare.ui.MyTripsActivity
import com.example.cabshare.ui.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        askNotificationPermission()
        loadUserProfile()
        updateFcmToken()

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
            auth.signOut()
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

    private fun updateFcmToken() {
        val uid = auth.currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "Your Token is: $token")
                db.collection("users").document(uid).update("fcmToken", token)
            } else {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        binding.tvHomeName.text = it.name
                        if (!it.profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(it.profileImageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .circleCrop()
                                .into(binding.ivHomeProfile)
                        }
                    }
                }
            }
    }
}
