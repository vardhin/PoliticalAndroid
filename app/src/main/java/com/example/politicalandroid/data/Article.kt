package com.example.politicalandroid.data

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