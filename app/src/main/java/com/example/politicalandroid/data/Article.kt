package com.example.politicalandroid.data

import android.net.Uri

data class Article(
    val articleId: Int,
    val title: String,
    val summary: String,
    val category: String,
    val date: String
)

data class DisplayArticle(
    val id: Int,
    val title: String,
    val excerpt: String,
    val imageUrl: String,
    val category: String,
    val date: String
)

data class DetailedArticle(
    val id: Int,
    val title: String,
    val excerpt: String,
    val content: String,
    val imageUrl: String,
    val category: String,
    val date: String,
    val featured: Boolean = false  // Add featured field
)

data class EditableArticle(
    val id: Int,
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val date: String,
    val featured: Boolean = false  // Add featured field
)