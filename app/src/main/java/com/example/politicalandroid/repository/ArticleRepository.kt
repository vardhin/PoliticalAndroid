package com.example.politicalandroid.repository

import com.example.politicalandroid.data.Article
import com.example.politicalandroid.data.DisplayArticle
import com.example.politicalandroid.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class ArticleRepository {
    private val apiService: ApiService
    
    companion object {
        private const val BASE_URL = "https://politicalgossips.vercel.app/api/"
    }
    
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ApiService::class.java)
    }
    
    suspend fun getFeaturedArticles(): Result<List<DisplayArticle>> {
        return try {
            val response = apiService.getFeaturedArticles()
            if (response.isSuccessful) {
                val articles = response.body()?.map { article ->
                    DisplayArticle(
                        id = article.articleId,
                        title = article.title,
                        excerpt = article.summary,
                        imageUrl = getImageUrl(article.articleId),
                        category = article.category,
                        date = formatDate(article.date)
                    )
                } ?: emptyList()
                Result.success(articles)
            } else {
                Result.failure(Exception("Failed to fetch featured articles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLatestArticles(): Result<List<DisplayArticle>> {
        return try {
            val response = apiService.getLatestArticles(4)
            if (response.isSuccessful) {
                val articles = response.body()?.map { article ->
                    DisplayArticle(
                        id = article.articleId,
                        title = article.title,
                        excerpt = article.summary,
                        imageUrl = getImageUrl(article.articleId),
                        category = article.category,
                        date = formatDate(article.date)
                    )
                } ?: emptyList()
                Result.success(articles)
            } else {
                Result.failure(Exception("Failed to fetch latest articles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getImageUrl(articleId: Int): String {
        return "${BASE_URL}image/$articleId"
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}