package com.github.jacklt.githubsample.data

data class RepositoryItem(
    val name: String,
    val description: String?,
    val url: String,
    val fork: Boolean,
    val stars: Int
)