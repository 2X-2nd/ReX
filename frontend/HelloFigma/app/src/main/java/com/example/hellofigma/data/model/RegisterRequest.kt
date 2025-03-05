package com.example.hellofigma.data.model


data class RegisterRequest(
    val google_id: String,
    val email: String,
    val username: String,
    val preferences: List<String>,
    val latitude: Double,
    val longitude: Double
)
