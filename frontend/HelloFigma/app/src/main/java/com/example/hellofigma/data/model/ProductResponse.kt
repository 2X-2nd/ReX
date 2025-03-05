package com.example.weather_dashboard.data.models


// Product Network data Model
data class Product(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val seller_id: String,
    val latitude: Double,
    val longitude: Double,
    val image: String,
    val created_at: String
)

data class ProductResponse(
    val results: List<Product>
)

data class ItemResponse(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val seller_id: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<String>,
    val created_at: String
)