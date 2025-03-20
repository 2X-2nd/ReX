package com.example.hellofigma.data.repository

import com.example.weather_dashboard.data.models.PostPriceSuggestionsRequest
import com.example.weather_dashboard.data.models.PostPriceSuggestionsResponse
import retrofit2.http.Body
import retrofit2.http.POST


interface PriceSuggestionsApi {
    @POST("price-suggestions")
    suspend fun postPriceSuggestions(
        @Body postPriceSuggestionsRequest: PostPriceSuggestionsRequest
    ): PostPriceSuggestionsResponse
}
