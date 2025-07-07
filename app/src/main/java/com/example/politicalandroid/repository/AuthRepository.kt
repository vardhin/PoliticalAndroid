package com.example.politicalandroid.repository

import com.example.politicalandroid.data.*
import com.example.politicalandroid.network.AuthApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {
    private val authApiService: AuthApiService
    
    companion object {
        private const val BASE_URL = "https://politicalgossips.vercel.app/api/"
    }
    
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()
        
        authApiService = retrofit.create(AuthApiService::class.java)
    }
    
    suspend fun checkBackendHealth(): Result<String> {
        return try {
            val response = authApiService.checkHealth()
            if (response.isSuccessful) {
                val health = response.body()
                val status = if (health?.dbState != null) {
                    "Backend: ${health.status}, DB: ${health.dbState}"
                } else {
                    "Backend: ${health?.status ?: "connected"}"
                }
                Result.success(status)
            } else {
                Result.failure(Exception("Backend unavailable"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Backend unavailable"))
        }
    }
    
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    Result.success(loginResponse)
                } ?: Result.failure(Exception("Invalid response"))
            } else {
                val errorMessage = try {
                    // Parse the actual error response from backend
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        // Try to parse JSON error response
                        val gson = com.google.gson.Gson()
                        val apiError = gson.fromJson(errorBody, ApiError::class.java)
                        apiError.message
                    } else {
                        "Login failed"
                    }
                } catch (e: Exception) {
                    "Login failed: ${response.code()} ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}