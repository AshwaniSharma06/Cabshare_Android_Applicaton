package com.example.cabshare.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cabshare.R
import com.example.cabshare.databinding.ActivityCreateTripBinding
import com.example.cabshare.viewmodel.CreateTripViewModel
import java.text.SimpleDateFormat
import java.util.*

class CreateTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateTripBinding
    private val viewModel: CreateTripViewModel by viewModels()
    
    private var selectedDate = ""
    private var selectedTime = ""
    private var seatCount = 1
    
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupObservers()
        setupListeners()
        viewModel.fetchCurrentUser()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.tripPosted.observe(this) { posted ->
            if (posted) {
                Toast.makeText(this, "Trip Posted Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Open Map Selector for both clicking the EditText or the End Icon
        binding.etStartingLocation.isFocusable = false
        binding.etStartingLocation.setOnClickListener {
            openMapSelector("Select Pickup", selectPickupLauncher)
        }
        binding.tilStartingLocation.setEndIconOnClickListener {
            openMapSelector("Select Pickup", selectPickupLauncher)
        }

        binding.etDestination.isFocusable = false
        binding.etDestination.setOnClickListener {
            openMapSelector("Select Destination", selectDestLauncher)
        }
        binding.tilDestination.setEndIconOnClickListener {
            openMapSelector("Select Destination", selectDestLauncher)
        }
        
        binding.etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = String.format("%02d/%02d/%d", day, month + 1, year)
                binding.etDate.setText(selectedDate)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val timeCalendar = Calendar.getInstance()
                timeCalendar.set(Calendar.HOUR_OF_DAY, hour)
                timeCalendar.set(Calendar.MINUTE, minute)
                
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                selectedTime = sdf.format(timeCalendar.time)
                binding.etTime.setText(selectedTime)
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
        }

        binding.btnMinusSeat.setOnClickListener {
            if (seatCount > 1) {
                seatCount--
                binding.tvSeatCount.text = seatCount.toString()
            }
        }

        binding.btnPlusSeat.setOnClickListener {
            if (seatCount < 7) {
                seatCount++
                binding.tvSeatCount.text = seatCount.toString()
            }
        }

        binding.btnSubmitTrip.setOnClickListener {
            submitTrip()
        }
    }

    private fun openMapSelector(hint: String, launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(this, SelectLocationActivity::class.java)
        intent.putExtra("hint", hint)
        launcher.launch(intent)
    }

    private fun submitTrip() {
        val startingLoc = binding.etStartingLocation.text.toString().trim()
        val destination = binding.etDestination.text.toString().trim()
        val fare = binding.etFare.text.toString().trim()
        val seatsStr = seatCount.toString()
        val isAC = binding.switchAC.isChecked
        
        val genderPreference = when (binding.toggleGender.checkedButtonId) {
            R.id.btnGenderMale -> "Male only"
            R.id.btnGenderFemale -> "Female only"
            else -> "Any"
        }
        
        if (validateInput(startingLoc, destination, fare)) {
            viewModel.submitTrip(
                startingLoc, destination, pickupLat, pickupLng, 
                destLat, destLng, selectedDate, selectedTime, fare, seatsStr,
                isAC, genderPreference
            )
        }
    }

    private fun validateInput(startingLoc: String, destination: String, fare: String): Boolean {
        var isValid = true

        if (startingLoc.isEmpty()) {
            binding.etStartingLocation.error = "Required"
            isValid = false
        }
        if (destination.isEmpty()) {
            binding.etDestination.error = "Required"
            isValid = false
        }
        if (selectedDate.isEmpty()) {
            binding.etDate.error = "Required"
            isValid = false
        }
        if (selectedTime.isEmpty()) {
            binding.etTime.error = "Required"
            isValid = false
        }
        if (fare.isEmpty()) {
            binding.etFare.error = "Required"
            isValid = false
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmitTrip.isEnabled = !isLoading
    }
}
