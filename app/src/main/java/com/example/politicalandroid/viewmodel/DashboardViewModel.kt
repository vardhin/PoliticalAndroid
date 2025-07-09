package com.example.politicalandroid.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.politicalandroid.data.Article
import com.example.politicalandroid.data.PreferencesManager
import com.example.politicalandroid.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isSubmitting: Boolean = false,
    val submitMessage: String? = null,
    val errorMessage: String? = null,
    val submitSuccess: Boolean = false
)

class DashboardViewModel(context: Context) : ViewModel() {
    private val repository = DashboardRepository()
    private val preferencesManager = PreferencesManager(context)
    private val context = context
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    fun createArticle(
        title: String,
        summary: String,
        articleText: String,
        category: String,
        featured: Boolean,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                submitMessage = null,
                errorMessage = null
            )
            
            try {
                val authToken = preferencesManager.authToken.first()
                if (authToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Please login again to continue",
                        submitSuccess = false
                    )
                    return@launch
                }
                
                val result = repository.createArticle(
                    title = title,
                    summary = summary,
                    articleText = articleText,
                    category = category,
                    featured = featured,
                    imageUri = imageUri,
                    context = context,
                    authToken = authToken
                )
                
                result.fold(
                    onSuccess = { article ->
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            submitMessage = "Article '${article.title}' published successfully!",
                            submitSuccess = true
                        )
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("401") == true || 
                            exception.message?.contains("Unauthorized") == true -> 
                                "Session expired. Please login again."
                            else -> exception.message ?: "Failed to publish article"
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            errorMessage = errorMessage,
                            submitSuccess = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Error: ${e.message}",
                    submitSuccess = false
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            submitMessage = null,
            errorMessage = null
        )
    }
}