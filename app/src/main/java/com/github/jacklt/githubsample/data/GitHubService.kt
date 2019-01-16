package com.github.jacklt.githubsample.data

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubService {
    // alternative (for generic users): @GET("users/{userId}/repos")
    @GET("orgs/{organizationId}/repos")
    fun organizationRepos(@Path("organizationId") organizationId: String, @Query("page") page: Int? = null): Deferred<List<RepositoryResponse>>
}