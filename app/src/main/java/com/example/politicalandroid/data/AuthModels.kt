package com.example.politicalandroid.data

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,  // Add missing refreshToken
    val user: User
)

data class User(
    val id: String,        // Changed from Int to String for MongoDB ObjectId
    val username: String,
    val role: String       // Changed from email to role
)

data class ApiError(
    val message: String,
    val error: String? = null
)

data class HealthResponse(
    val status: String,
    val dbState: String? = null
)