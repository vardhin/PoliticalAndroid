package com.example.politicalandroid.repository

import android.content.Context
import android.net.Uri
import com.example.politicalandroid.data.*
import com.example.politicalandroid.network.ArticleDetailApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class ArticleDetailRepository {
    private val apiService: ArticleDetailApiService
    
    companion object {
        private const val BASE_URL = "https://politicalgossips.vercel.app/api/"
    }
    
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ArticleDetailApiService::class.java)
    }
    
    suspend fun deleteArticle(articleId: Int, authToken: String): Result<Unit> {
        return try {
            // Ensure the token has the Bearer prefix
            val formattedToken = if (authToken.startsWith("Bearer ")) {
                authToken
            } else {
                "Bearer $authToken"
            }
            
            println("Attempting to delete article $articleId with token: ${formattedToken.take(20)}...")
            
            val response = apiService.deleteArticle(
                articleId = articleId,
                token = formattedToken
            )
            
            println("Delete response: ${response.code()}")
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                println("Delete error: $errorMessage")
                
                when (response.code()) {
                    401 -> Result.failure(Exception("Authentication failed. Please login again."))
                    403 -> Result.failure(Exception("You don't have permission to delete this article."))
                    404 -> Result.failure(Exception("Article not found."))
                    else -> Result.failure(Exception("Failed to delete article: ${response.code()} - $errorMessage"))
                }
            }
        } catch (e: Exception) {
            println("Delete exception: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    suspend fun updateArticle(
        editableArticle: EditableArticle,
        imageUri: Uri?,
        authToken: String,
        context: Context
    ): Result<Unit> {
        return try {
            // Create multipart request body
            val titleBody = editableArticle.title.toRequestBody("text/plain".toMediaTypeOrNull())
            val summaryBody = editableArticle.summary.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentBody = editableArticle.content.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = editableArticle.category.toRequestBody("text/plain".toMediaTypeOrNull())
            val dateBody = editableArticle.date.toRequestBody("text/plain".toMediaTypeOrNull())
            val featuredBody = editableArticle.featured.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            var imagePart: MultipartBody.Part? = null
            imageUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                } catch (e: Exception) {
                    // Handle image error but continue with text updates
                    println("Error processing image: ${e.message}")
                }
            }
            
            val response = if (imagePart != null) {
                apiService.updateArticleWithImage(
                    articleId = editableArticle.id,
                    token = "Bearer $authToken",
                    title = titleBody,
                    summary = summaryBody,
                    content = contentBody,
                    category = categoryBody,
                    date = dateBody,
                    featured = featuredBody,
                    image = imagePart
                )
            } else {
                apiService.updateArticle(
                    articleId = editableArticle.id,
                    token = "Bearer $authToken",
                    title = titleBody,
                    summary = summaryBody,
                    content = contentBody,
                    category = categoryBody,
                    date = dateBody,
                    featured = featuredBody
                )
            }
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed to update article: ${response.code()} - $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    suspend fun getArticleById(articleId: Int): Result<DetailedArticle> {
        return try {
            val response = apiService.getArticleById(articleId)
            if (response.isSuccessful) {
                val article = response.body()
                if (article != null) {
                    val detailedArticle = DetailedArticle(
                        id = article.articleId,
                        title = article.title,
                        excerpt = article.summary,
                        content = article.article_text ?: "No content available",
                        imageUrl = "${BASE_URL}image/${article.articleId}",
                        category = article.category,
                        date = formatDate(article.date),
                        featured = article.featured ?: false  // Handle featured status
                    )
                    Result.success(detailedArticle)
                } else {
                    Result.failure(Exception("Article not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch article: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}