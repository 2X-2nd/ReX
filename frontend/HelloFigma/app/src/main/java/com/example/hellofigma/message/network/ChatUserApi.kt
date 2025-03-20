package com.example.hellofigma.message.repository

import com.example.weather_dashboard.data.models.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path


// Retrofit接口
interface ChatUserApi {
    @GET("users/{id}")
    suspend fun getUser(
        @Path("id") id: String
    ): UserResponse
}
