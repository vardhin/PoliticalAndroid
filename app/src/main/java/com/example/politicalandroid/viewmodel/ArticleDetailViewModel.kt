package com.example.politicalandroid.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.politicalandroid.data.DetailedArticle
import com.example.politicalandroid.data.EditableArticle
import com.example.politicalandroid.data.PreferencesManager
import com.example.politicalandroid.repository.ArticleDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ArticleDetailUiState(
    val article: DetailedArticle? = null,
    val editableArticle: EditableArticle? = null,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val hasChanges: Boolean = false,
    val selectedImageUri: Uri? = null,
    val errorMessage: String? = null,
    val saveMessage: String? = null,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val requiresReauth: Boolean = false
)

class ArticleDetailViewModel(context: Context) : ViewModel() {
    private val repository = ArticleDetailRepository()
    private val preferencesManager = PreferencesManager(context)
    private val context = context
    
    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()
    
    fun loadArticle(articleId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = repository.getArticleById(articleId)
            result.fold(
                onSuccess = { article ->
                    _uiState.value = _uiState.value.copy(
                        article = article,
                        editableArticle = EditableArticle(
                            id = article.id,
                            title = article.title,
                            summary = article.excerpt,
                            content = article.content,
                            category = article.category,
                            date = article.date,
                            featured = article.featured  // Make sure to include featured status
                        ),
                        isLoading = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load article"
                    )
                }
            )
        }
    }
    
    fun showDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }
    
    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }
    
    fun deleteArticle() {
        val articleId = _uiState.value.article?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true, 
                showDeleteConfirm = false,
                saveMessage = null,
                requiresReauth = false
            )
            
            try {
                val authToken = preferencesManager.authToken.first()
                println("Auth token retrieved: ${authToken?.take(20)}...")
                
                if (authToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        saveMessage = "No authentication token found. Please login again.",
                        saveSuccess = false,
                        requiresReauth = true
                    )
                    return@launch
                }
                
                val result = repository.deleteArticle(articleId, authToken)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            deleteSuccess = true,
                            saveMessage = "Article deleted successfully!",
                            saveSuccess = true
                        )
                    },
                    onFailure = { exception ->
                        val isAuthError = exception.message?.contains("Authentication failed") == true ||
                                        exception.message?.contains("Invalid token") == true
                        
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            saveMessage = exception.message ?: "Failed to delete article",
                            saveSuccess = false,
                            requiresReauth = isAuthError
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    saveMessage = "Error: ${e.message}",
                    saveSuccess = false
                )
            }
        }
    }
    
    fun clearAuthError() {
        _uiState.value = _uiState.value.copy(requiresReauth = false)
    }
    
    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode,
            saveMessage = null
        )
        if (!_uiState.value.isEditMode) {
            resetChanges()
        }
    }
    
    fun updateTitle(title: String) {
        val current = _uiState.value.editableArticle ?: return
        _uiState.value = _uiState.value.copy(
            editableArticle = current.copy(title = title),
            hasChanges = true,
            saveMessage = null
        )
    }
    
    fun updateSummary(summary: String) {
        val current = _uiState.value.editableArticle ?: return
        _uiState.value = _uiState.value.copy(
            editableArticle = current.copy(summary = summary),
            hasChanges = true,
            saveMessage = null
        )
    }
    
    fun updateContent(content: String) {
        val current = _uiState.value.editableArticle ?: return
        _uiState.value = _uiState.value.copy(
            editableArticle = current.copy(content = content),
            hasChanges = true,
            saveMessage = null
        )
    }
    
    fun updateCategory(category: String) {
        val current = _uiState.value.editableArticle ?: return
        _uiState.value = _uiState.value.copy(
            editableArticle = current.copy(category = category),
            hasChanges = true,
            saveMessage = null
        )
    }
    
    fun updateDate(date: String) {
        val current = _uiState.value.editableArticle ?: return
        _uiState.value = _uiState.value.copy(
            editableArticle = current.copy(date = date),
            hasChanges = true,
            saveMessage = null
        )
    }
    
    fun updateFeatured(featured: Boolean) {
        val current = _uiState.value.editableArticle ?: return
        _uiState.value = _uiState.value.copy(
            editableArticle = current.copy(featured = featured),
            hasChanges = true,
            saveMessage = null
        )
    }
    
    fun updateSelectedImage(uri: Uri, context: Context) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            hasChanges = true,
            saveMessage = null
        )
    }
    
    fun resetChanges() {
        val original = _uiState.value.article ?: return
        _uiState.value = _uiState.value.copy(
            editableArticle = EditableArticle(
                id = original.id,
                title = original.title,
                summary = original.excerpt,
                content = original.content,
                category = original.category,
                date = original.date,
                featured = original.featured  // Include featured in reset
            ),
            selectedImageUri = null,
            hasChanges = false,
            saveMessage = null
        )
    }
    
    fun saveChanges() {
        val editableArticle = _uiState.value.editableArticle ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveMessage = null)
            
            try {
                val authToken = preferencesManager.authToken.first()
                if (authToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveMessage = "Authentication required",
                        saveSuccess = false
                    )
                    return@launch
                }
                
                val result = repository.updateArticle(
                    editableArticle,
                    _uiState.value.selectedImageUri,
                    authToken,
                    context
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            hasChanges = false,
                            isEditMode = false,
                            saveMessage = "Article updated successfully!",
                            saveSuccess = true,
                            selectedImageUri = null
                        )
                        // Reload the article to get updated data
                        loadArticle(editableArticle.id)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            saveMessage = exception.message ?: "Failed to save changes",
                            saveSuccess = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveMessage = "Error: ${e.message}",
                    saveSuccess = false
                )
            }
        }
    }
}