package com.example.cabshare.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.example.cabshare.databinding.DialogRatingBinding
import com.google.firebase.firestore.FirebaseFirestore

class RatingDialog(
    context: Context,
    private val driverId: String,
    private val tripId: String,
    private val onRatingSubmitted: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogRatingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmitRating.setOnClickListener {
            val rating = binding.ratingBar.rating
            val comment = binding.etRatingComment.text.toString()

            if (rating == 0f) {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitRating(rating, comment)
        }

        binding.btnCancelRating.setOnClickListener {
            dismiss()
        }
    }

    private fun submitRating(rating: Float, comment: String) {
        val db = FirebaseFirestore.getInstance()
        val ratingData = hashMapOf(
            "rating" to rating,
            "comment" to comment,
            "driverId" to driverId,
            "tripId" to tripId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("ratings").add(ratingData)
            .addOnSuccessListener {
                Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                updateDriverAverageRating(rating)
                onRatingSubmitted()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error submitting rating", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDriverAverageRating(newRating: Float) {
        val db = FirebaseFirestore.getInstance()
        val driverRef = db.collection("users").document(driverId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(driverRef)
            val currentRating = snapshot.getDouble("rating") ?: 5.0
            val totalRatings = snapshot.getLong("totalRatings") ?: 0L
            
            val newTotalRatings = totalRatings + 1
            val newAvgRating = ((currentRating * totalRatings) + newRating) / newTotalRatings
            
            transaction.update(driverRef, "rating", newAvgRating)
            transaction.update(driverRef, "totalRatings", newTotalRatings)
            null
        }
    }
}
