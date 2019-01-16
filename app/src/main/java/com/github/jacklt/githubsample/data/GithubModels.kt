package com.github.jacklt.githubsample.data

data class RepositoryResponse(
    val name: String?,
    val full_name: String?,
    val html_url: String?,
    val description: String?,
    val fork: Boolean?,
    val stargazers_count: Int?,
    val language: String?
) {
    fun toAppItem() = RepositoryItem(
        name = name ?: throw IllegalStateException("Missing name"),
        description = description,
        url = html_url ?: throw IllegalStateException("Missing html_url"),
        fork = fork ?: false,
        stars = stargazers_count ?: 0
    )
}