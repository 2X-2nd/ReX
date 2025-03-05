package com.example.hellofigma.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hellofigma.data.model.RegisterRequest
import com.example.hellofigma.data.repository.DataStoreManager
import com.example.hellofigma.data.repository.LoginState
import com.example.hellofigma.data.repository.ProductRepository
import com.example.hellofigma.data.repository.Result
import com.example.weather_dashboard.data.models.DeleteProductResponse
import com.example.weather_dashboard.data.models.ItemResponse
import com.example.weather_dashboard.data.models.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ItemViewModel @Inject constructor(
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

    private val _uiState = MutableStateFlow<Result<ItemResponse>>(Result.Loading)
    val uiState: StateFlow<Result<ItemResponse>> = _uiState

    fun getProduct(id: String) {
        viewModelScope.launch {
            repository.getProduct(id)
                .collect { result ->
                    _uiState.value = result
                }
        }
    }

    private val _deleteState = MutableStateFlow<DeleteProductResponse?>(null)
    val deleteState: StateFlow<DeleteProductResponse?> = _deleteState
    fun deleteProduct(id: String) {
        viewModelScope.launch {
            _deleteState.value = null;
            _deleteState.value = repository.deleteProduct(id)
        }
    }

    suspend fun getUser(google_id: String): UserResponse? {
        return repository.getUser(google_id)
    }
}