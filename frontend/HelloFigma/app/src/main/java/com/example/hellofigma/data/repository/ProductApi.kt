package com.example.hellofigma.data.repository

import com.example.weather_dashboard.data.models.DeleteProductResponse
import com.example.weather_dashboard.data.models.ItemResponse
import com.example.weather_dashboard.data.models.PostPriceSuggestionsRequest
import com.example.weather_dashboard.data.models.PostPriceSuggestionsResponse
import com.example.weather_dashboard.data.models.PostProductRequest
import com.example.weather_dashboard.data.models.PostProductResponse
import com.example.weather_dashboard.data.models.ProductResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ProductApi {
    @GET("listings")
    suspend fun queryProduct(
        @Query("query") query: String
    ): ProductResponse

    @GET("listings/search")
    suspend fun searchProduct(
        @Query("query") query: String
    ): ProductResponse

    @GET("listings")
    suspend fun getProduct(
        @Query("id") id: String
    ): ItemResponse

    @DELETE("listings/{id}")
    suspend fun deleteProduct(
        @Path("id") id: String
    ): DeleteProductResponse

    @POST("listings")
    suspend fun postProduct(
        @Body postProductRequest: PostProductRequest
    ): PostProductResponse

    @POST("price-suggestions")
    suspend fun postPriceSuggestions(
        @Body postPriceSuggestionsRequest: PostPriceSuggestionsRequest
    ): PostPriceSuggestionsResponse
}
