package com.example.hellofigma.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hellofigma.data.repository.DataStoreManager
import com.example.hellofigma.data.repository.LoginState
import com.example.hellofigma.data.repository.ProductRepository
import com.example.weather_dashboard.data.models.PostPriceSuggestionsRequest
import com.example.weather_dashboard.data.models.PostPriceSuggestionsResponse
import com.example.weather_dashboard.data.models.PostProductRequest
import com.example.weather_dashboard.data.models.PostProductResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    val loginState: StateFlow<LoginState> = dataStoreManager.loginState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoginState(
                isLoggedIn = false,
                googleId = null,
                userName = null,
                userEmail = null
            )
        )

    private val _postProductResult = MutableStateFlow<PostProductResponse?>(null)
    val postProductResult: StateFlow<PostProductResponse?> = _postProductResult
    fun postProduct(postProductRequest: PostProductRequest) {
        viewModelScope.launch {
            _postProductResult.value = null;
            _postProductResult.value = repository.postProduct(postProductRequest)
        }
    }

    private val _postPriceSuggestionsResponseResult = MutableStateFlow<PostPriceSuggestionsResponse?>(null)
    val postPriceSuggestionsResponseResult: StateFlow<PostPriceSuggestionsResponse?> = _postPriceSuggestionsResponseResult
    fun PostPriceSuggestions(postPriceSuggestionsRequest: PostPriceSuggestionsRequest) {
        viewModelScope.launch {
            _postPriceSuggestionsResponseResult.value = null;
            _postPriceSuggestionsResponseResult.value = repository.PostPriceSuggestions(postPriceSuggestionsRequest)
        }
    }
}