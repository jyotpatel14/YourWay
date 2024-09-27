package com.example.yourway.forum
data class Comment(
    val id: String = "",
    val content: String = "",
    val upvotes: Int = 0,
    val userId: String = "", // User ID of the person who made the comment
    val parentId: String? = null // ID of the parent comment, if it's a reply
)

