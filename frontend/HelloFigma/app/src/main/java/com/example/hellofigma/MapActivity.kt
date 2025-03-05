package com.example.hellofigma

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnConfirmLocation: Button
    private var userLocationMarker: Marker? = null

    private val warehouseLocations = mapOf(
        "UBC" to LatLng(49.2606, -123.2460),
        "Richmond" to LatLng(49.1666, -123.1336),
        "Burnaby" to LatLng(49.2500, -122.9500)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnConfirmLocation = findViewById(R.id.btnConfirmLocation)
        btnConfirmLocation.setOnClickListener {
            finish()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // add warehouse markers
        warehouseLocations.forEach { (name, latLng) ->
            googleMap.addMarker(MarkerOptions().position(latLng).title(name))
        }

        googleMap.uiSettings.isZoomControlsEnabled = true

        // **点击地图上的仓库或 Your Location**
        googleMap.setOnMarkerClickListener { marker ->
            val selectedLocation = marker.title
            if (selectedLocation == "Your Location") {
                returnSelectedYourLocation()
            } else {
                returnSelectedWarehouse(selectedLocation)
            }
            true
        }

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableUserLocation()
            } else {
                Toast.makeText(this, "Location access denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getUserLocation()
        }
    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // give permission for location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    addUserLocationMarker(location)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        } else {
            // unable to get permission for location
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    // add your location marker
    private fun addUserLocationMarker(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)

        // remove previous marker
        userLocationMarker?.remove()

        // add new marker
        userLocationMarker = googleMap.addMarker(
            MarkerOptions().position(userLatLng).title("Your Location").icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        )

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
    }

    // click warehouse and return
    private fun returnSelectedWarehouse(warehouseName: String?) {
        val resultIntent = Intent().apply {
            putExtra("selectedWarehouse", warehouseName)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    // choose your location and return
    private fun returnSelectedYourLocation() {
        val userLatLng = userLocationMarker?.position
        if (userLatLng != null) {
            val resultIntent = Intent().apply {
                putExtra("selectedYourLocation", "GPS location")
            }
            setResult(RESULT_OK, resultIntent)
        }
        finish()
    }


}
