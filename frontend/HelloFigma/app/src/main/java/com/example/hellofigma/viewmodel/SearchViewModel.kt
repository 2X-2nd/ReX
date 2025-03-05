package com.example.hellofigma.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hellofigma.data.repository.ProductRepository
import com.example.hellofigma.data.repository.Result
import com.example.weather_dashboard.data.models.ProductResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {
    private val _uiState = mutableStateOf<Result<ProductResponse>>(Result.Wait)
    val uiState: State<Result<ProductResponse>> = _uiState

    fun searchProduct(query: String) {
        viewModelScope.launch {
            repository.searchProduct(query)
                .collect { result ->
                    _uiState.value = result
                }
        }
    }
}
