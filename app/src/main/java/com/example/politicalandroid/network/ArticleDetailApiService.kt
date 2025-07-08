package com.example.politicalandroid.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class DetailedArticleResponse(
    val articleId: Int,
    val title: String,
    val summary: String,
    val article_text: String?,
    val category: String,
    val date: String,
    val featured: Boolean?  // Add featured field
)

interface ArticleDetailApiService {
    @GET("articles/{id}")
    suspend fun getArticleById(@Path("id") articleId: Int): Response<DetailedArticleResponse>
    
    @Multipart
    @PUT("articles/{id}")
    suspend fun updateArticleWithImage(
        @Path("id") articleId: Int,
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("summary") summary: RequestBody,
        @Part("article_text") content: RequestBody,
        @Part("category") category: RequestBody,
        @Part("date") date: RequestBody,
        @Part("featured") featured: RequestBody,  // Add featured field
        @Part image: MultipartBody.Part
    ): Response<Unit>
    
    @Multipart
    @PUT("articles/{id}")
    suspend fun updateArticle(
        @Path("id") articleId: Int,
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("summary") summary: RequestBody,
        @Part("article_text") content: RequestBody,
        @Part("category") category: RequestBody,
        @Part("date") date: RequestBody,
        @Part("featured") featured: RequestBody  // Add featured field
    ): Response<Unit>
    
    @DELETE("articles/{id}")
    suspend fun deleteArticle(
        @Path("id") articleId: Int,
        @Header("Authorization") token: String
    ): Response<Unit>
}