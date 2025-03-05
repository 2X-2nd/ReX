package com.example.weather_dashboard.data.models


// User Network data Model
data class UserResponse(
    val google_id: String,
    val email: String,
    val username: String,
    val preferences: List<String>,
    val latitude: Double,
    val longitude: Double
)
