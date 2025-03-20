package com.example.hellofigma

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.IOException
import java.util.Locale

class StorageActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocationMarker: Marker? = null
    private lateinit var btnConfirmLocation: Button

    private val warehouseLocations = mapOf(
        "UBC Warehouse" to LatLng(49.2606, -123.2460),
        "Richmond Warehouse" to LatLng(49.1666, -123.1336),
        "Burnaby Warehouse" to LatLng(49.2500, -122.9500)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // close map
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation)
        btnConfirmLocation.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // **添加仓库标记**
        warehouseLocations.forEach { (name, latLng) ->
            googleMap.addMarker(MarkerOptions().position(latLng).title(name))
        }

        googleMap.uiSettings.isZoomControlsEnabled = true

        // **点击地图上的仓库**
        googleMap.setOnMarkerClickListener { marker ->
            val selectedWarehouse = marker.title
            if (selectedWarehouse == "Your Location") {
                Toast.makeText(this, "This is your location", Toast.LENGTH_SHORT).show()
            } else {
                openWarehouseDetails(selectedWarehouse ?: "")
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
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
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
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    addUserLocationMarker(location)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUserLocationMarker(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)

        userLocationMarker?.remove()  // 移除旧的用户位置钉

        userLocationMarker = googleMap.addMarker(
            MarkerOptions().position(userLatLng).title("Your Location").icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        )

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
    }

    private fun openWarehouseDetails(warehouseName: String) {
        val warehouseLatLng = warehouseLocations[warehouseName]
        if (warehouseLatLng != null) {
            val address = getAddressFromLocation(warehouseLatLng.latitude, warehouseLatLng.longitude)

            val intent = Intent(this, WarehouseDetailsActivity::class.java).apply {
                putExtra("WAREHOUSE_NAME", warehouseName)
                putExtra("WAREHOUSE_ADDRESS", address)
            }
            startActivity(intent)
        }
    }

    private fun getAddressFromLocation(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) // 获取完整地址
            } else {
                "Unknown Location"
            }
        } catch (e: IOException) { // 处理网络或 GPS 服务不可用的情况
            Log.e("LocationService", "Network or GPS error while fetching address", e)
            "Unknown Location"
        } catch (e: IllegalArgumentException) { // 处理无效的经纬度
            Log.e("LocationService", "Invalid latitude or longitude: ($lat, $lng)", e)
            "Invalid Location"
        }
    }
}
