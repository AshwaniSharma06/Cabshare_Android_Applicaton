package com.example.cabshare.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cabshare.databinding.ActivityCreateTripBinding
import com.example.cabshare.model.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateTripBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                binding.tvSelectedDate.text = selectedDate
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnPickTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                binding.tvSelectedTime.text = selectedTime
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        binding.btnSubmitTrip.setOnClickListener {
            val destination = binding.etDestination.text.toString()
            val fare = binding.etFare.text.toString()
            val userId = auth.currentUser?.uid ?: ""
            val userName = auth.currentUser?.displayName ?: "Anonymous"

            if (destination.isNotEmpty() && selectedDate.isNotEmpty() && selectedTime.isNotEmpty() && fare.isNotEmpty()) {
                val tripId = db.collection("trips").document().id
                val trip = Trip(tripId, userId, userName, destination, selectedDate, selectedTime, fare)
                
                db.collection("trips").document(tripId).set(trip)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Trip Posted Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error posting trip", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
