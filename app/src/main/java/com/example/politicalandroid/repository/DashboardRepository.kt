package com.example.politicalandroid.repository

import android.content.Context
import android.net.Uri
import com.example.politicalandroid.data.Article
import com.example.politicalandroid.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DashboardRepository {
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
    
    suspend fun createArticle(
        title: String,
        summary: String,
        articleText: String,
        category: String,
        featured: Boolean,
        imageUri: Uri,
        context: Context,
        authToken: String
    ): Result<Article> {
        return try {
            // Convert URI to file
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val file = File(context.cacheDir, "article_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            // Create multipart request body
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val summaryBody = summary.toRequestBody("text/plain".toMediaTypeOrNull())
            val articleTextBody = articleText.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
            val featuredBody = featured.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Format current date
            val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            val dateBody = currentDate.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val response = apiService.createArticle(
                token = "Bearer $authToken",
                title = titleBody,
                summary = summaryBody,
                articleText = articleTextBody,
                category = categoryBody,
                date = dateBody,
                featured = featuredBody,
                image = imagePart
            )
            
            // Clean up temp file
            file.delete()
            
            if (response.isSuccessful) {
                val article = response.body()
                if (article != null) {
                    Result.success(article)
                } else {
                    Result.failure(Exception("Invalid response from server"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed to create article: ${response.code()} - $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}