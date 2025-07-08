package com.example.politicalandroid.repository

import com.example.politicalandroid.data.ContactSubmissionsResponse
import com.example.politicalandroid.network.ContactApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ContactRepository {
    private val apiService: ContactApiService
    
    companion object {
        private const val BASE_URL = "https://politicalgossips.vercel.app/api/"
    }
    
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ContactApiService::class.java)
    }
    
    suspend fun getContactSubmissions(token: String, page: Int = 1, limit: Int = 20): Result<ContactSubmissionsResponse> {
        return try {
            // Ensure the token has the Bearer prefix
            val formattedToken = if (token.startsWith("Bearer ")) {
                token
            } else {
                "Bearer $token"
            }
            
            val response = apiService.getContactSubmissions(formattedToken, page, limit)
            if (response.isSuccessful) {
                response.body()?.let { submissionsResponse ->
                    Result.success(submissionsResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                when (response.code()) {
                    401 -> Result.failure(Exception("Authentication failed. Please login again."))
                    403 -> Result.failure(Exception("You don't have permission to view contact submissions."))
                    else -> Result.failure(Exception("Failed to fetch contact submissions: ${response.code()} - $errorMessage"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}