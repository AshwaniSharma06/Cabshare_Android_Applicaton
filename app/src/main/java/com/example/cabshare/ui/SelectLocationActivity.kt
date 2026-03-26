package com.example.cabshare.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cabshare.R
import com.example.cabshare.adapter.LocationSuggestionAdapter
import com.example.cabshare.databinding.ActivitySelectLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivitySelectLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var suggestionAdapter: LocationSuggestionAdapter
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String = ""
    private var searchJob: Job? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupSearch()
        setupRecyclerView()

        binding.btnConfirmLocation.setOnClickListener {
            if (selectedLatLng != null) {
                val resultIntent = Intent()
                resultIntent.putExtra("address", selectedAddress)
                resultIntent.putExtra("latitude", selectedLatLng!!.latitude)
                resultIntent.putExtra("longitude", selectedLatLng!!.longitude)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        suggestionAdapter = LocationSuggestionAdapter(emptyList()) { address ->
            onSuggestionSelected(address)
        }
        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = suggestionAdapter
    }

    private fun onSuggestionSelected(address: Address) {
        val latLng = LatLng(address.latitude, address.longitude)
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title(address.featureName))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        
        selectedLatLng = latLng
        selectedAddress = address.getAddressLine(0) ?: "Unknown Location"
        binding.tvSelectedAddress.text = selectedAddress
        
        binding.rvSuggestions.visibility = View.GONE
        binding.etSearch.setText(address.featureName)
        
        // Hide keyboard
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                searchJob?.cancel()
                if (query.length > 2) {
                    searchJob = lifecycleScope.launch {
                        delay(500) // Debounce
                        updateSuggestions(query)
                    }
                } else {
                    binding.rvSuggestions.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchLocation(query)
                }
                true
            } else {
                false
            }
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchLocation(query)
            }
        }
    }

    private suspend fun updateSuggestions(query: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addressList = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // This is still a bit tricky as the new API is callback based, 
                    // but we can use the old one on IO thread for now as it's common practice
                    geocoder.getFromLocationName(query, 5)
                } else {
                    geocoder.getFromLocationName(query, 5)
                }
            }
            
            if (!addressList.isNullOrEmpty()) {
                suggestionAdapter.updateSuggestions(addressList)
                binding.rvSuggestions.visibility = View.VISIBLE
            } else {
                binding.rvSuggestions.visibility = View.GONE
            }
        } catch (e: Exception) {
            binding.rvSuggestions.visibility = View.GONE
        }
    }

    private fun searchLocation(query: String) {
        lifecycleScope.launch {
            val geocoder = Geocoder(this@SelectLocationActivity, Locale.getDefault())
            try {
                val addressList = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(query, 1)
                }
                if (!addressList.isNullOrEmpty()) {
                    onSuggestionSelected(addressList[0])
                } else {
                    Toast.makeText(this@SelectLocationActivity, "Location not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SelectLocationActivity, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            selectedLatLng = latLng
            updateAddress(latLng)
            binding.rvSuggestions.visibility = View.GONE
        }

        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
                    selectedLatLng = currentLatLng
                    updateAddress(currentLatLng)
                } else {
                    Log.d("Map", "Location is null, falling back to Delhi")
                    val delhi = LatLng(28.6139, 77.2090)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, 12f))
                }
            }
            .addOnFailureListener {
                Log.e("Map", "Error getting location", it)
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    private fun updateAddress(latLng: LatLng) {
        lifecycleScope.launch {
            val geocoder = Geocoder(this@SelectLocationActivity, Locale.getDefault())
            try {
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                }
                if (!addresses.isNullOrEmpty()) {
                    selectedAddress = addresses[0].getAddressLine(0)
                    binding.tvSelectedAddress.text = selectedAddress
                }
            } catch (e: Exception) {
                binding.tvSelectedAddress.text = "Lat: ${latLng.latitude}, Lon: ${latLng.longitude}"
            }
        }
    }
}
