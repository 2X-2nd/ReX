package com.example.hellofigma

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hellofigma.data.repository.LoginState
import com.example.hellofigma.viewmodel.PostViewModel
import com.example.weather_dashboard.data.models.PostPriceSuggestionsRequest
import com.example.weather_dashboard.data.models.PostProductRequest
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


@AndroidEntryPoint
class PostActivity : AppCompatActivity() {
    private val viewModel: PostViewModel by viewModels()
    private var currentLoginState: LoginState = LoginState(false,"","", "")
    private lateinit var location: Location

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var tvSuggestedPrice: TextView
    private lateinit var warehouseOptions: LinearLayout
    private lateinit var radioGroupWarehouse: RadioGroup
    private lateinit var radioYourLocation: RadioButton
    private lateinit var radioWarehouse: RadioButton
    private lateinit var radioManualAddress: RadioButton
    private lateinit var radioUBC: RadioButton
    private lateinit var radioRichmond: RadioButton
    private lateinit var radioBurnaby: RadioButton
    private lateinit var btnPost: Button
    private lateinit var btnSelectLocation: Button
    private lateinit var tvUserLocation: TextView
    private lateinit var etManualAddress: EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //image
    private lateinit var imageView: ImageView
    private var imageBase64: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { newState ->
                    currentLoginState = newState
                }
            }
        }

        val content = this
        lifecycleScope.launch {
            viewModel.postProductResult.collect { response ->
                if (response != null) {
                    if (response.id === "") {
                        Toast.makeText(content, response.message, Toast.LENGTH_SHORT).show()
                    } else {
                        val resultIntent = Intent().apply {
                            putExtra("REFRESH_NEEDED", true)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
        }

        // 初始化 Spinner
        val spinnerCategory: Spinner = findViewById(R.id.spinnerCategory)
        val categories = listOf("Cell Phone", "PC", "Tablet", "Fridge", "Laundry", "TV", "Bed", "Desk", "Storage", "Tops", "Bottoms", "Shoes", "Other")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCategory.adapter = adapter
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // **初始化 UI 组件**
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etPrice = findViewById(R.id.etPrice)
        tvSuggestedPrice = findViewById(R.id.tvSuggestedPrice)

        warehouseOptions = findViewById(R.id.warehouseOptions)
        radioGroupWarehouse = findViewById(R.id.radioGroupWarehouse)
        radioYourLocation = findViewById(R.id.radioYourLocation)
        radioWarehouse = findViewById(R.id.radioWarehouse)
        radioManualAddress = findViewById(R.id.radioManualAddress)
        radioUBC = findViewById(R.id.radioUBC)
        radioRichmond = findViewById(R.id.radioRichmond)
        radioBurnaby = findViewById(R.id.radioBurnaby)
        btnPost = findViewById(R.id.btnPost)
        btnSelectLocation = findViewById(R.id.btnSelectLocation)
        tvUserLocation = findViewById(R.id.tvUserLocation)
        etManualAddress = findViewById(R.id.etManualAddress)
        //image
        imageView = findViewById(R.id.btnAddImage)

        etTitle.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val title = etTitle.text.toString().trim()
                if (title.isEmpty()) {
                    etTitle.error = "Title is required"
                } else {
                    etTitle.error = null

                    viewModel.PostPriceSuggestions(PostPriceSuggestionsRequest(keyword = title))
                }
            } else {
                tvSuggestedPrice.text = ""
            }
        }

        lifecycleScope.launch {
            viewModel.postPriceSuggestionsResponseResult.collect { response ->
                if (response != null) {
                    tvSuggestedPrice.text = "Suggested: $%.2f".format(response.best_price)
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        imageView.setOnClickListener {
            selectImageFromGallery()
        }

        // **选择地图地址**
        btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("gpsLocation", tvUserLocation.text.toString()) // 传递 GPS 地址
            mapLauncher.launch(intent)
        }

        // **Radio 按钮互斥**
        radioYourLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioWarehouse.isChecked = false
                radioManualAddress.isChecked = false
                warehouseOptions.visibility = LinearLayout.GONE
                etManualAddress.setText("")
                etManualAddress.clearFocus()
            }
        }

        radioWarehouse.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioYourLocation.isChecked = false
                radioManualAddress.isChecked = false
                warehouseOptions.visibility = LinearLayout.VISIBLE
                etManualAddress.setText("")
                etManualAddress.clearFocus()
            } else {
                warehouseOptions.visibility = LinearLayout.GONE
                radioGroupWarehouse.clearCheck()
            }
        }

        radioManualAddress.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioYourLocation.isChecked = false
                radioWarehouse.isChecked = false
                warehouseOptions.visibility = LinearLayout.GONE
            }
        }

        // **手动输入时，自动取消其他选项**
        etManualAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    radioYourLocation.isChecked = false
                    radioWarehouse.isChecked = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnPost.setOnClickListener {
            val finalAddress = when {
                etManualAddress.text.toString().isNotEmpty() -> etManualAddress.text.toString()
                radioUBC.isChecked -> "UBC"
                radioRichmond.isChecked -> "Richmond"
                radioBurnaby.isChecked -> "Burnaby"
                radioYourLocation.isChecked -> tvUserLocation.text.toString()
                else -> "No location selected"
            }
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val price = etPrice.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                etDescription.error = "Description is required"
                return@setOnClickListener
            }

            if (price.isEmpty()) {
                etPrice.error = "Price is required"
                return@setOnClickListener
            }

            if (imageBase64.isEmpty()) {
                etTitle.error = "Image is required"
                return@setOnClickListener
            }

            // Toast.makeText(this, "Posted: $title - $description", Toast.LENGTH_SHORT).show()
            // Toast.makeText(this, "Posted with Location: $finalAddress", Toast.LENGTH_SHORT).show()

            if (!currentLoginState.isLoggedIn) {
                Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                viewModel.postProduct(
                    PostProductRequest(
                        title = title,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        seller_id = currentLoginState.googleId ?: "",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        images = listOf("data:image/png;base64,$imageBase64", "test.jpg")
                    )
                )
            }

        }
    }

    // **监听地图返回的仓库**
    private val mapLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedWarehouse = result.data?.getStringExtra("selectedWarehouse")
                val selectedYourLocation = result.data?.getStringExtra("selectedYourLocation")

                when {
                    selectedWarehouse != null -> {
                        radioWarehouse.isChecked = true
                        warehouseOptions.visibility = LinearLayout.VISIBLE
                        when (selectedWarehouse) {
                            "UBC" -> radioUBC.isChecked = true
                            "Richmond" -> radioRichmond.isChecked = true
                            "Burnaby" -> radioBurnaby.isChecked = true
                        }
                    }
                    selectedYourLocation != null -> {
                        radioYourLocation.isChecked = true
                        warehouseOptions.visibility = LinearLayout.GONE
                    }
                }
            }
        }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getUserLocation()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getUserLocation()
            } else {
                Toast.makeText(this, "Location access denied", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                updateUserLocation(location)
            } else {
                requestNewLocation()
            }
        }.addOnFailureListener {
            tvUserLocation.text = "Failed to get location"
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.firstOrNull()?.let { location ->
                    updateUserLocation(location)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun updateUserLocation(location: Location) {
        this.location = location
        lifecycleScope.launch(Dispatchers.IO) { // 在 IO 线程执行耗时操作
            val address = try {
                getAddressFromLocation(location.latitude, location.longitude)
            } catch (e: Exception) {
                "Unknown Location"
            }
            withContext(Dispatchers.Main) { // 切换回主线程更新 UI
                tvUserLocation.text = address
            }
        }
        //this.location = location
        //val address = getAddressFromLocation(location.latitude, location.longitude)
        //tvUserLocation.text = address
    }

    private fun getAddressFromLocation(lat: Double, lng: Double): String {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            return geocoder.getFromLocation(lat, lng, 1)?.get(0)?.getAddressLine(0)
                ?: "Unknown Location"
        } catch (e: Exception) {
            return "Unknown Location"
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // 处理选择的图片
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val base64 = uriToBase64(context = this, uri = uri)
                    // 存储 Base64 字符串
                    imageBase64 = base64 ?: ""
                    imageView.setImageURI(uri) // 显示选中的图片
                }
            }
        }

    private fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() // 读取全部字节
            Base64.encodeToString(bytes, Base64.DEFAULT) // 编码为 Base64
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
