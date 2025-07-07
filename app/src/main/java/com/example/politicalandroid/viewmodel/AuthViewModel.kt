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
                preferencesManager.userData
            ) { token, user ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = !token.isNullOrEmpty() && user != null,
                    user = user
                )
            }.collect()
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
                    preferencesManager.saveAuthData(loginResponse.accessToken, loginResponse.user)
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