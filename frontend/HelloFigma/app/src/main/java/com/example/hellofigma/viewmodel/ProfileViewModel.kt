package com.example.hellofigma.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hellofigma.data.model.RegisterRequest
import com.example.hellofigma.data.repository.DataStoreManager
import com.example.hellofigma.data.repository.LoginState
import com.example.hellofigma.data.repository.ProductRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
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

    suspend fun loginOut() {
        dataStoreManager.clearLoginState()
    }

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

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let {
                userRegister(
                    RegisterRequest(
                        google_id = account.id!!,
                        email = account.email!!,
                        username = account.displayName!!,
                        preferences = listOf("", ""),
                        latitude = 1.0,
                        longitude = 1.0
                    )
                )
                userLogin(account.id!!)
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Login fail: ${e.statusCode}")
        }
    }
}