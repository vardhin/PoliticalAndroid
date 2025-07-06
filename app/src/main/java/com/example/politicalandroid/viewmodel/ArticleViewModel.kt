package com.example.politicalandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.politicalandroid.data.DisplayArticle
import com.example.politicalandroid.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ArticleUiState(
    val featuredArticles: List<DisplayArticle> = emptyList(),
    val latestArticles: List<DisplayArticle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ArticleViewModel : ViewModel() {
    private val repository = ArticleRepository()
    
    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()
    
    init {
        loadArticles()
    }
    
    fun loadArticles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Load featured articles
                val featuredResult = repository.getFeaturedArticles()
                val latestResult = repository.getLatestArticles()
                
                val featured = featuredResult.getOrElse { emptyList() }
                val latest = latestResult.getOrElse { emptyList() }
                
                val errorMessage = when {
                    featuredResult.isFailure && latestResult.isFailure -> "Failed to load articles"
                    featuredResult.isFailure -> "Failed to load featured articles"
                    latestResult.isFailure -> "Failed to load latest articles"
                    else -> null
                }
                
                _uiState.value = ArticleUiState(
                    featuredArticles = featured,
                    latestArticles = latest,
                    isLoading = false,
                    errorMessage = errorMessage
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load content: ${e.message}"
                )
            }
        }
    }
}