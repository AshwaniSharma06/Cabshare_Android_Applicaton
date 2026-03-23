package com.example.cabshare.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    
    private var pickupLat = 0.0
    private var pickupLng = 0.0
    private var destLat = 0.0
    private var destLng = 0.0

    private val selectPickupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val address = result.data?.getStringExtra("address")
            pickupLat = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            pickupLng = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            if (address != null) {
                binding.etStartingLocation.setText(address)
            }
        }
    }

    private val selectDestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val address = result.data?.getStringExtra("address")
            destLat = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            destLng = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            if (address != null) {
                binding.etDestination.setText(address)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupSeatSpinner()

        binding.btnOpenMapPickup.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            selectPickupLauncher.launch(intent)
        }

        binding.btnOpenMapDest.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            selectDestLauncher.launch(intent)
        }

        binding.btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                binding.tvSelectedDate.text = selectedDate
                binding.tvSelectedDate.error = null
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnPickTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                binding.tvSelectedTime.text = selectedTime
                binding.tvSelectedTime.error = null
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        binding.btnSubmitTrip.setOnClickListener {
            val startingLoc = binding.etStartingLocation.text.toString().trim()
            val destination = binding.etDestination.text.toString().trim()
            val fare = binding.etFare.text.toString().trim()
            val seats = binding.spinnerSeats.selectedItem.toString().toInt()
            
            if (validateInput(startingLoc, destination, fare)) {
                showLoading(true)
                val userId = auth.currentUser?.uid ?: ""
                val userName = auth.currentUser?.displayName ?: "User"

                val tripId = db.collection("trips").document().id
                val trip = Trip(
                    tripId = tripId,
                    userId = userId,
                    userName = userName,
                    startingLocation = startingLoc,
                    destination = destination,
                    pickupLat = pickupLat,
                    pickupLng = pickupLng,
                    destLat = destLat,
                    destLng = destLng,
                    date = selectedDate,
                    time = selectedTime,
                    fare = fare,
                    totalSeats = seats,
                    availableSeats = seats
                )
                
                db.collection("trips").document(tripId).set(trip)
                    .addOnSuccessListener {
                        showLoading(false)
                        Toast.makeText(this, "Trip Posted Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        Toast.makeText(this, "Error posting trip: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun setupSeatSpinner() {
        val seatOptions = arrayOf("1", "2", "3", "4", "5", "6")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, seatOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSeats.adapter = adapter
        binding.spinnerSeats.setSelection(3) // Default to 4 seats
    }

    private fun validateInput(startingLoc: String, destination: String, fare: String): Boolean {
        var isValid = true

        if (startingLoc.isEmpty()) {
            binding.etStartingLocation.error = "Starting location is required"
            isValid = false
        }
        if (destination.isEmpty()) {
            binding.etDestination.error = "Destination is required"
            isValid = false
        }
        if (selectedDate.isEmpty()) {
            binding.tvSelectedDate.error = "Please select a date"
            isValid = false
        }
        if (selectedTime.isEmpty()) {
            binding.tvSelectedTime.error = "Please select a time"
            isValid = false
        }
        if (fare.isEmpty()) {
            binding.etFare.error = "Fare is required"
            isValid = false
        } else {
            try {
                fare.toDouble()
            } catch (e: NumberFormatException) {
                binding.etFare.error = "Enter a valid amount"
                isValid = false
            }
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmitTrip.isEnabled = !isLoading
        binding.etStartingLocation.isEnabled = !isLoading
        binding.etDestination.isEnabled = !isLoading
        binding.etFare.isEnabled = !isLoading
        binding.btnPickDate.isEnabled = !isLoading
        binding.btnPickTime.isEnabled = !isLoading
    }
}
