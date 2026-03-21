package com.example.cabshare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cabshare.auth.LoginActivity
import com.example.cabshare.databinding.ActivityMainBinding
import com.example.cabshare.ui.CreateTripActivity
import com.example.cabshare.ui.FindTripActivity
import com.example.cabshare.ui.ProfileActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnCreateTrip.setOnClickListener {
            startActivity(Intent(this, CreateTripActivity::class.java))
        }

        binding.btnFindTrip.setOnClickListener {
            startActivity(Intent(this, FindTripActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
