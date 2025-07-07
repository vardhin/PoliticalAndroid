package com.example.politicalandroid.network

import com.example.politicalandroid.data.HealthResponse
import com.example.politicalandroid.data.LoginRequest
import com.example.politicalandroid.data.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}