package com.example.cabshare.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cabshare.MainActivity
import com.example.cabshare.R
import com.example.cabshare.auth.LoginActivity
import com.example.cabshare.databinding.ActivitySplashBinding
import com.example.cabshare.viewmodel.SplashViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load Animations
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        // Using standard slide_in_bottom or similar if custom slide_up is missing
        val slideUp = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_slide_in_bottom)

        // Apply Animations
        binding.ivLogo.startAnimation(fadeIn)
        binding.tvAppName.startAnimation(slideUp)
        binding.tvTagline.startAnimation(slideUp)

        setupObservers()

        // Delay for 3 seconds and navigate
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.checkUserStatus()
        }, 3000)
    }

    private fun setupObservers() {
        viewModel.isUserLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }
    }
}
