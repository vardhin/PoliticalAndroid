package com.example.politicalandroid.network

import com.example.politicalandroid.data.Article
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("articles/featured")
    suspend fun getFeaturedArticles(): Response<List<Article>>
    
    @GET("articles/latest")
    suspend fun getLatestArticles(@Query("limit") limit: Int = 4): Response<List<Article>>
    
    @Multipart
    @POST("articles")
    suspend fun createArticle(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("summary") summary: RequestBody,
        @Part("article_text") articleText: RequestBody,
        @Part("category") category: RequestBody,
        @Part("date") date: RequestBody,
        @Part("featured") featured: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<Article>
}