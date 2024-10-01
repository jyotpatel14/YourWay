package com.example.yourway.fetchpost
data class Comment(
    val commentId: String,
    val content: String,
    val likes: Int,
    val timestamp: Long,
    val userId: String
)
