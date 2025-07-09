package com.example.politicalandroid.data

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String
)

data class User(
    val id: String,
    val username: String,
    val role: String
)

data class ApiError(
    val message: String,
    val error: String? = null
)

data class HealthResponse(
    val status: String,
    val dbState: String? = null
)