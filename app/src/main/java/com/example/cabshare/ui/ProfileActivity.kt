package com.example.cabshare.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cabshare.R
import com.example.cabshare.databinding.ActivityProfileBinding
import com.example.cabshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
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

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserProfile()

        binding.fabEditPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        showLoading(true)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        binding.tvProfileName.text = it.name
                        binding.tvProfileEmail.text = it.email
                        binding.rbUserRating.rating = it.rating
                        binding.tvRatingText.text = "Rating: ${String.format("%.1f", it.rating)}"
                        binding.etBio.setText(it.bio)
                        
                        if (!it.profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(it.profileImageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(binding.ivProfile)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val bio = binding.etBio.text.toString()
        
        showLoading(true)
        
        if (selectedImageUri != null) {
            // Upload image first
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        updateUserInFirestore(uid, bio, url.toString())
                    }
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Just update bio
            updateUserInFirestore(uid, bio, null)
        }
    }

    private fun updateUserInFirestore(uid: String, bio: String, imageUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "bio" to bio
        )
        if (imageUrl != null) {
            updates["profileImageUrl"] = imageUrl
        }

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !isLoading
    }
}
