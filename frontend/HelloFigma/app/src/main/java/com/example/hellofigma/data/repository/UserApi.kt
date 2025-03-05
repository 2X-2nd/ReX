package com.example.hellofigma.data.repository

import com.example.hellofigma.data.model.RegisterRequest
import com.example.weather_dashboard.data.models.RegisterResponse
import com.example.weather_dashboard.data.models.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface UserApi {
    @POST("/users/register")
    suspend fun userRegister(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @GET("users/{id}")
    suspend fun getUser(
        @Path("id") id: String
    ): UserResponse
}
