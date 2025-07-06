package com.example.politicalandroid.network

import com.example.politicalandroid.data.Article
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("articles/featured")
    suspend fun getFeaturedArticles(): Response<List<Article>>
    
    @GET("articles/latest")
    suspend fun getLatestArticles(@Query("limit") limit: Int = 4): Response<List<Article>>
}