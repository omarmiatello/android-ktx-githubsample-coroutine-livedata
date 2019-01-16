package com.github.jacklt.githubsample.data

import com.github.jacklt.githubsample.ServicesConfig
import com.github.jacklt.githubsample.mockData
import com.github.jacklt.githubsample.services
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

class GitHubServiceTest {
    var mockServer: MockWebServer by Delegates.notNull()

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        val okHttpClient = OkHttpClient()

        ServicesConfig.buildInstance(
            ServicesConfig(
                okHttpClient,
                Retrofit.Builder()
                    .baseUrl(mockServer.url("/"))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .client(okHttpClient)
                    .build()
                    .create(GitHubService::class.java),
                logD = { println("Log: $it") })
        )
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    private fun enqueueMockResponseFrom(filename: String) {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockData(filename))
        )
    }

    @Test
    fun organizationRepos_parseResponse() {
        val organizationId = "test"
        enqueueMockResponseFrom("orgs_gdgmilano_repos_1.json")

        val response = runBlocking { services.github.organizationRepos(organizationId).await() }

        assertEquals(5, response.size)
    }

    @Test
    fun organizationRepos_pageParameter() {
        val organizationId = "test"
        enqueueMockResponseFrom("orgs_github_repos_1.json")
        enqueueMockResponseFrom("orgs_github_repos_1.json")
        enqueueMockResponseFrom("orgs_github_repos_2.json")

        runBlocking {
            services.github.organizationRepos(organizationId).await()
            services.github.organizationRepos(organizationId, 1).await()
            services.github.organizationRepos(organizationId, 2).await()
        }

        assertEquals("GET /orgs/$organizationId/repos HTTP/1.1", mockServer.takeRequest().requestLine)
        assertEquals("GET /orgs/$organizationId/repos?page=1 HTTP/1.1", mockServer.takeRequest().requestLine)
        assertEquals("GET /orgs/$organizationId/repos?page=2 HTTP/1.1", mockServer.takeRequest().requestLine)
    }
}