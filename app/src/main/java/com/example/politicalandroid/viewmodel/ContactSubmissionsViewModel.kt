package com.example.politicalandroid.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.politicalandroid.data.ContactSubmission
import com.example.politicalandroid.data.PaginationInfo
import com.example.politicalandroid.data.PreferencesManager
import com.example.politicalandroid.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ContactSubmissionsUiState(
    val submissions: List<ContactSubmission> = emptyList(),
    val pagination: PaginationInfo? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ContactSubmissionsViewModel(context: Context) : ViewModel() {
    private val repository = ContactRepository()
    private val preferencesManager = PreferencesManager(context)
    
    private val _uiState = MutableStateFlow(ContactSubmissionsUiState())
    val uiState: StateFlow<ContactSubmissionsUiState> = _uiState.asStateFlow()
    
    fun loadContactSubmissions(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val authToken = preferencesManager.authToken.first()
                if (authToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Authentication required"
                    )
                    return@launch
                }
                
                val result = repository.getContactSubmissions(authToken, page, limit)
                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            submissions = response.submissions,
                            pagination = response.pagination,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load contact submissions"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }
}