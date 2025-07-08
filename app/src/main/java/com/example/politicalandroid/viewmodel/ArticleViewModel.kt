package com.example.politicalandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.politicalandroid.data.DisplayArticle
import com.example.politicalandroid.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class ArticleUiState(
    val featuredArticles: List<DisplayArticle> = emptyList(),
    val latestArticles: List<DisplayArticle> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val hasContent: Boolean = false
)

class ArticleViewModel : ViewModel() {
    private val repository = ArticleRepository()
    
    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()
    
    private var retryAttempts = 0
    private val maxRetryAttempts = 5
    private val retryDelayMs = 3000L // 3 seconds
    
    init {
        loadArticles()
    }
    
    fun loadArticles(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            
            tryLoadArticles(isRefresh)
        }
    }
    
    private suspend fun tryLoadArticles(isRefresh: Boolean) {
        try {
            // Load featured articles
            val featuredResult = repository.getFeaturedArticles()
            val latestResult = repository.getLatestArticles()
            
            val featured = featuredResult.getOrElse { emptyList() }
            val latest = latestResult.getOrElse { emptyList() }
            
            val hasError = featuredResult.isFailure || latestResult.isFailure
            val hasContent = featured.isNotEmpty() || latest.isNotEmpty()
            
            if (hasError && !hasContent) {
                // If we have an error and no content, retry silently
                if (retryAttempts < maxRetryAttempts) {
                    retryAttempts++
                    delay(retryDelayMs)
                    tryLoadArticles(isRefresh)
                    return
                } else {
                    // Max retries reached, show error
                    val errorMessage = when {
                        featuredResult.isFailure && latestResult.isFailure -> "Failed to load articles"
                        featuredResult.isFailure -> "Failed to load featured articles"
                        latestResult.isFailure -> "Failed to load latest articles"
                        else -> null
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = errorMessage
                    )
                    retryAttempts = 0
                    return
                }
            }
            
            // Success - reset retry attempts
            retryAttempts = 0
            
            _uiState.value = ArticleUiState(
                featuredArticles = featured,
                latestArticles = latest,
                isLoading = false,
                isRefreshing = false,
                errorMessage = null,
                hasContent = hasContent
            )
            
        } catch (e: Exception) {
            // Network or other exception
            if (retryAttempts < maxRetryAttempts && !_uiState.value.hasContent) {
                retryAttempts++
                delay(retryDelayMs)
                tryLoadArticles(isRefresh)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = if (_uiState.value.hasContent) null else "Failed to load content: ${e.message}"
                )
                retryAttempts = 0
            }
        }
    }
    
    fun refresh() {
        loadArticles(isRefresh = true)
    }
}