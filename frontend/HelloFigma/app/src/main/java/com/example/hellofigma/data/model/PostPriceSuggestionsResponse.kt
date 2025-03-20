package com.example.weather_dashboard.data.models


/*
 Post Price Suggestions Network data Model
 */
data class PostPriceSuggestionsResponse(
    val best_price: Double,
    val similar_items: List<SimilarItem>
)

data class SimilarItem(
    val name: String,
    val price: Double,
    val url: String
)
