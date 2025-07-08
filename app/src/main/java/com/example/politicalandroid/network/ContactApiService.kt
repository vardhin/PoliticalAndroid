package com.example.politicalandroid.network

import com.example.politicalandroid.data.ContactSubmissionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ContactApiService {
    @GET("admin/contact-submissions")
    suspend fun getContactSubmissions(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ContactSubmissionsResponse>
}