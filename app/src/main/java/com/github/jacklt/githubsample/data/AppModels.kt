package com.github.jacklt.githubsample.data

import androidx.annotation.Keep

@Keep
data class RepositoryItem(
    val name: String,
    val description: String?,
    val url: String,
    val fork: Boolean,
    val stars: Int
)