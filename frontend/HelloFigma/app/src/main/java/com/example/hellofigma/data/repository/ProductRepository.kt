package com.example.hellofigma.data.repository

import com.example.hellofigma.data.model.RegisterRequest
import com.example.weather_dashboard.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import java.io.IOException
import retrofit2.HttpException
import com.google.gson.JsonParseException
import android.util.Log

class ProductRepository @Inject constructor(
    private val productApi: ProductApi,
    private val priceSuggestionsApi: PriceSuggestionsApi,
    private val userApi: UserApi,
) {

    // ** 统一 API 调用的错误处理 **
    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): T? {
        return try {
            apiCall()
        } catch (e: IOException) { // 处理网络错误
            Log.e("ProductRepository", "Network error: ${e.message}", e)
            null
        } catch (e: HttpException) { // 处理 HTTP 错误
            Log.e("ProductRepository", "HTTP error: ${e.code()}", e)
            null
        } catch (e: JsonParseException) { // 处理 JSON 解析错误
            Log.e("ProductRepository", "JSON parsing error", e)
            null
        }
    }

    suspend fun userRegister(registerRequest: RegisterRequest): RegisterResponse? =
        safeApiCall { userApi.userRegister(registerRequest) }

    suspend fun getUser(google_id: String): UserResponse? =
        safeApiCall { userApi.getUser(google_id) }

    suspend fun queryProduct(query: String): Flow<Result<ProductResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = productApi.queryProduct(query)
            emit(Result.Success(response))
        } catch (e: IOException) {
            Log.e("ProductRepository", "Network error: ${e.message}", e)
            emit(Result.Error(e))
        } catch (e: HttpException) {
            Log.e("ProductRepository", "HTTP error: ${e.code()}", e)
            emit(Result.Error(e))
        } catch (e: JsonParseException) {
            Log.e("ProductRepository", "JSON parsing error", e)
            emit(Result.Error(e))
        }
    }

    suspend fun getProduct(id: String): Flow<Result<ItemResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = productApi.getProduct(id)
            emit(Result.Success(response))
        } catch (e: IOException) {
            Log.e("ProductRepository", "Network error: ${e.message}", e)
            emit(Result.Error(e))
        } catch (e: HttpException) {
            Log.e("ProductRepository", "HTTP error: ${e.code()}", e)
            emit(Result.Error(e))
        } catch (e: JsonParseException) {
            Log.e("ProductRepository", "JSON parsing error", e)
            emit(Result.Error(e))
        }
    }

    suspend fun searchProduct(query: String): Flow<Result<ProductResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = productApi.searchProduct(query)
            emit(Result.Success(response))
        } catch (e: IOException) {
            Log.e("ProductRepository", "Network error: ${e.message}", e)
            emit(Result.Error(e))
        } catch (e: HttpException) {
            Log.e("ProductRepository", "HTTP error: ${e.code()}", e)
            emit(Result.Error(e))
        } catch (e: JsonParseException) {
            Log.e("ProductRepository", "JSON parsing error", e)
            emit(Result.Error(e))
        }
    }

    suspend fun deleteProduct(id: String): DeleteProductResponse? =
        safeApiCall { productApi.deleteProduct(id) } ?: DeleteProductResponse(message = "Delete failed")

    suspend fun postProduct(postProductRequest: PostProductRequest): PostProductResponse? =
        safeApiCall { productApi.postProduct(postProductRequest) } ?: PostProductResponse(id = "", message = "POST - Server error!")

    suspend fun PostPriceSuggestions(postPriceSuggestionsRequest: PostPriceSuggestionsRequest): PostPriceSuggestionsResponse? =
        safeApiCall { priceSuggestionsApi.postPriceSuggestions(postPriceSuggestionsRequest) } ?: PostPriceSuggestionsResponse(best_price = 0.0, similar_items = emptyList())
}

sealed class Result<out T> {
    data object Wait : Result<Nothing>()
    data object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
