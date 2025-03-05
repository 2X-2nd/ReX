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
import com.example.weather_dashboard.data.models.ProductResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProductViewModel @Inject constructor(
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

    private val _uiState = mutableStateOf<Result<ProductResponse>>(Result.Loading)
    val uiState: State<Result<ProductResponse>> = _uiState

    fun userRegister(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            repository.userRegister(registerRequest)
        }
    }

    fun userLogin(google_id: String) {
        viewModelScope.launch {
            val user = repository.getUser(google_id)
            if (user != null) {
                dataStoreManager.saveLoginState(user.google_id, user.username, user.email)
            }
        }
    }

    fun queryProduct(query: String) {
        viewModelScope.launch {
            repository.queryProduct(query)
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
}