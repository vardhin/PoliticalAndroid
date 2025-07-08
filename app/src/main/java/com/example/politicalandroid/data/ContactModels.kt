package com.example.politicalandroid.data

data class ContactSubmission(
    val _id: String,
    val name: String,
    val email: String,
    val message: String,
    val timestamp: String,
    val ipAddress: String?,
    val userAgent: String?
)

data class ContactSubmissionsResponse(
    val submissions: List<ContactSubmission>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val current: Int,
    val pages: Int,
    val total: Int
)