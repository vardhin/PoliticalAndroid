package com.example.politicalandroid.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.politicalandroid.data.PreferencesManager
import com.example.politicalandroid.data.User
import com.example.politicalandroid.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val backendStatus: String = "Checking...",
    val loginSuccess: Boolean = false
)

class AuthViewModel(context: Context) : ViewModel() {
    private val repository = AuthRepository()
    private val preferencesManager = PreferencesManager(context)
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthStatus()
        checkBackendHealth()
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            combine(
                preferencesManager.authToken,
                preferencesManager.refreshToken,
                preferencesManager.userData
            ) { token, refreshToken, user ->
                val isLoggedIn = (!token.isNullOrEmpty() || !refreshToken.isNullOrEmpty()) && user != null
                
                // Try to refresh token if we have a refresh token but no access token
                if (token.isNullOrEmpty() && !refreshToken.isNullOrEmpty() && user != null) {
                    tryRefreshToken(refreshToken)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = isLoggedIn,
                    user = user
                )
            }.collect()
        }
    }
    
    private fun tryRefreshToken(refreshToken: String) {
        viewModelScope.launch {
            val result = repository.refreshToken(refreshToken)
            result.fold(
                onSuccess = { newAccessToken ->
                    preferencesManager.updateAccessToken(newAccessToken)
                },
                onFailure = {
                    // If refresh fails, clear all auth data
                    preferencesManager.clearAuthData()
                }
            )
        }
    }
    
    private fun checkBackendHealth() {
        viewModelScope.launch {
            val result = repository.checkBackendHealth()
            _uiState.value = _uiState.value.copy(
                backendStatus = result.getOrElse { "Backend unavailable" }
            )
        }
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = repository.login(username, password)
            result.fold(
                onSuccess = { loginResponse ->
                    preferencesManager.saveAuthData(
                        loginResponse.accessToken, 
                        loginResponse.refreshToken, 
                        loginResponse.user
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true,
                        isLoggedIn = true,
                        user = loginResponse.user
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            preferencesManager.clearAuthData()
            _uiState.value = AuthUiState(backendStatus = _uiState.value.backendStatus)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearLoginSuccess() {
        _uiState.value = _uiState.value.copy(loginSuccess = false)
    }
}