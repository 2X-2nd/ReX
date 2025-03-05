package com.example.weather_dashboard.data.models


/*
 Post Product Network data Model
 */
data class PostProductRequest(
    val title: String,
    val description: String,
    val price: Double,
    val seller_id: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<String>
)
