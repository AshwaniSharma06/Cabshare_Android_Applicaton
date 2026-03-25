package com.example.cabshare.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.cabshare.R
import com.example.cabshare.auth.LoginActivity
import com.example.cabshare.databinding.ActivityProfileBinding
import com.example.cabshare.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivProfile.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply animations
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.profileImageContainer.startAnimation(fadeIn)
        binding.nameContainer.startAnimation(fadeIn)

        setupObservers()
        viewModel.fetchUserProfile()

        // Toolbar back navigation
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.fabEditPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.tvProfileName.text.toString()
            val bio = binding.etBio.text.toString()
            // Pass contentResolver to the viewModel for Base64 conversion
            viewModel.saveProfile(contentResolver, name, bio, selectedImageUri)
        }

        binding.cvBookingHistory.setOnClickListener {
            val intent = Intent(this, MyTripsActivity::class.java)
            intent.putExtra("SHOW_HISTORY", true)
            startActivity(intent)
        }

        binding.cvLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            user?.let {
                binding.tvProfileName.text = it.name
                binding.tvProfileEmail.text = it.email
                binding.rbUserRating.rating = it.rating
                binding.tvRatingValue.text = String.format("%.1f", it.rating)
                binding.tvTotalReviews.text = "${it.totalReviews} reviews"
                binding.tvTotalTrips.text = it.totalTrips.toString()
                binding.etBio.setText(it.bio)
                
                // Show verified badge if applicable
                binding.ivVerified.visibility = if (it.isVerified) View.VISIBLE else View.GONE

                // Format "Member Since" date
                it.memberSince?.let { timestamp ->
                    val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                    binding.tvMemberSince.text = sdf.format(timestamp.toDate())
                }
                
                // Glide automatically handles Base64 strings starting with "data:image/..."
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .into(binding.ivProfile)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.statusMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (it == "Profile updated successfully!") {
                    selectedImageUri = null 
                }
                viewModel.clearStatusMessage()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !isLoading
    }
}
