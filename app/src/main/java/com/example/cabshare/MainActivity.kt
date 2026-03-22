package com.example.cabshare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cabshare.auth.LoginActivity
import com.example.cabshare.databinding.ActivityMainBinding
import com.example.cabshare.model.User
import com.example.cabshare.ui.CreateTripActivity
import com.example.cabshare.ui.FindTripActivity
import com.example.cabshare.ui.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserProfile()

        binding.btnCreateTrip.setOnClickListener {
            startActivity(Intent(this, CreateTripActivity::class.java))
        }

        binding.btnFindTrip.setOnClickListener {
            startActivity(Intent(this, FindTripActivity::class.java))
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

    override fun onResume() {
        super.onResume()
        loadUserProfile() // Refresh name/photo if they changed in ProfileActivity
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
