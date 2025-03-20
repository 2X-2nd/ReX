package com.example.hellofigma.data.repository

import com.example.hellofigma.data.model.RegisterRequest
import com.example.weather_dashboard.data.models.DeleteProductResponse
import com.example.weather_dashboard.data.models.ItemResponse
import com.example.weather_dashboard.data.models.PostPriceSuggestionsRequest
import com.example.weather_dashboard.data.models.PostPriceSuggestionsResponse
import com.example.weather_dashboard.data.models.PostProductRequest
import com.example.weather_dashboard.data.models.PostProductResponse
import com.example.weather_dashboard.data.models.ProductResponse
import com.example.weather_dashboard.data.models.RegisterResponse
import com.example.weather_dashboard.data.models.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class ProductRepository @Inject constructor(
    private val productApi: ProductApi,
    private val priceSuggestionsApi: PriceSuggestionsApi,
    private val userApi: UserApi,
) {
    suspend fun userRegister(registerRequest: RegisterRequest): RegisterResponse? {
        try {
            return userApi.userRegister(registerRequest)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getUser(google_id: String): UserResponse? {
        try {
            return userApi.getUser(google_id)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun queryProduct(query: String): Flow<Result<ProductResponse>> = flow {
        emit(Result.Loading)
        try {
            // Fetch fresh data
            val response = productApi.queryProduct(query = query)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun getProduct(id: String): Flow<Result<ItemResponse>> = flow {
        emit(Result.Loading)
        try {
            // Fetch fresh data
            val response = productApi.getProduct(id = id)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun searchProduct(query: String): Flow<Result<ProductResponse>> = flow {
        emit(Result.Loading)
        try {
            // Fetch fresh data
            val response = productApi.searchProduct(query = query)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun deleteProduct(id: String): DeleteProductResponse? {
        try {
            val response = productApi.deleteProduct(id = id)
            return response
        } catch (e: Exception) {
            return DeleteProductResponse(message = "")
        }
    }

    suspend fun postProduct(postProductRequest: PostProductRequest): PostProductResponse? {
        try {
            val response = productApi.postProduct(postProductRequest = postProductRequest)
            return response
        } catch (e: Exception) {
            return PostProductResponse(id = "", message = "POST - Server error!")
        }
    }

    suspend fun PostPriceSuggestions(postPriceSuggestionsRequest: PostPriceSuggestionsRequest): PostPriceSuggestionsResponse? {
        try {
            val response = priceSuggestionsApi.postPriceSuggestions(postPriceSuggestionsRequest = postPriceSuggestionsRequest)
            return response
        } catch (e: Exception) {
            return PostPriceSuggestionsResponse(best_price = 0.0, similar_items = emptyList())
        }
    }
}

sealed class Result<out T> {
    data object Wait : Result<Nothing>()
    data object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
