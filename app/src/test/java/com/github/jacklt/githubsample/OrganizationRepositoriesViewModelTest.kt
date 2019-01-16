package com.github.jacklt.githubsample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.jacklt.githubsample.data.GitHubService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

class OrganizationRepositoriesViewModelTest {
    // https://github.com/Kotlin/kotlinx.coroutines/tree/master/core/kotlinx-coroutines-test
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private var mockServer: MockWebServer by Delegates.notNull()

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        val okHttpClient = OkHttpClient()
        Dispatchers.setMain(mainThreadSurrogate)

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
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        mockServer.shutdown()
    }

    private fun enqueueMockResponse(filename: String) {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockData(filename))
        )
    }

    @Test
    fun getAllRepo_singleRequest() {
        val organizationId = "test"
        enqueueMockResponse("orgs_gdgmilano_repos_1.json")
        val model = OrganizationRepositoriesViewModel()

        val response = runBlocking { model.getAllRepo(organizationId) }

        assertEquals(5, response.size)
        assertEquals(OrganizationRepositoriesViewModel.ResponseStatus.COMPLETED, model.bestReposStatus.value)
    }

    @Test
    fun getAllRepo_multipleRequest() {
        val organizationId = "test"
        enqueueMockResponse("orgs_github_repos_1.json")
        enqueueMockResponse("orgs_github_repos_2.json")
        enqueueMockResponse("orgs_github_repos_3.json")
        enqueueMockResponse("orgs_github_repos_4.json")
        enqueueMockResponse("orgs_github_repos_5.json")
        enqueueMockResponse("orgs_github_repos_6.json")
        enqueueMockResponse("orgs_github_repos_7.json")
        enqueueMockResponse("orgs_github_repos_8.json")
        enqueueMockResponse("orgs_github_repos_9.json")
        enqueueMockResponse("orgs_github_repos_10.json")
        val model = OrganizationRepositoriesViewModel()

        val response = runBlocking { model.getAllRepo(organizationId) }

        assertEquals(292, response.size)
        assertEquals(OrganizationRepositoriesViewModel.ResponseStatus.COMPLETED, model.bestReposStatus.value)
    }

    @Test
    fun getAllRepo_lastPageWith30Result() {
        val organizationId = "test"
        enqueueMockResponse("orgs_github_repos_1.json")     // 30 items
        enqueueMockResponse("orgs_github_repos_2.json")     // +30 items
        enqueueMockResponse("orgs_github_repos_11.json")    // +0 items (page 3 with empty list)
        val model = OrganizationRepositoriesViewModel()

        val response = runBlocking { model.getAllRepo(organizationId) }

        assertEquals(60, response.size)
        assertEquals(OrganizationRepositoriesViewModel.ResponseStatus.COMPLETED, model.bestReposStatus.value)
    }

    @Test
    fun getAllRepo_noResult_empty() {
        val organizationId = "test"
        enqueueMockResponse("orgs_github_repos_11.json")    // 0 items
        val model = OrganizationRepositoriesViewModel()

        val response = runBlocking { model.getAllRepo(organizationId) }

        assertEquals(0, response.size)
        assertEquals(OrganizationRepositoriesViewModel.ResponseStatus.NO_RESULT, model.bestReposStatus.value)
    }

    @Test
    fun getAllRepo_noResult_404() {
        val organizationId = "test"
        mockServer.enqueue(MockResponse().setResponseCode(404))
        val model = OrganizationRepositoriesViewModel()

        val response = runBlocking { model.getAllRepo(organizationId) }

        assertEquals(0, response.size)
        assertEquals(OrganizationRepositoriesViewModel.ResponseStatus.NO_RESULT, model.bestReposStatus.value)
    }

    @Test
    fun getAllRepo_noConnection() {
        val organizationId = "test"
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        val model = OrganizationRepositoriesViewModel()

        val response = runBlocking { model.getAllRepo(organizationId) }

        assertEquals(0, response.size)
        assertEquals(OrganizationRepositoriesViewModel.ResponseStatus.ERROR_IO, model.bestReposStatus.value)
    }

    @Test
    fun getAllRepo_serverError() {
        val organizationId = "test"
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val model = OrganizationRepositoriesViewModel()

        val response = runBlocking { model.getAllRepo(organizationId) }

        assertEquals(0, response.size)
        assertEquals(OrganizationRepositoriesViewModel.ResponseStatus.ERROR_OTHER, model.bestReposStatus.value)
    }

    @Test
    fun updateStatus_loading() {
        val model = OrganizationRepositoriesViewModel()
        model.updateStatus(OrganizationRepositoriesViewModel.ResponseStatus.LOADING)
        assert(model.bestReposLoading.value == true)
        assert(model.bestReposNoResult.value == false)
        assert(model.bestReposRetry.value == false)
    }

    @Test
    fun updateStatus_completed() {
        val model = OrganizationRepositoriesViewModel()
        model.updateStatus(OrganizationRepositoriesViewModel.ResponseStatus.COMPLETED)
        assert(model.bestReposLoading.value == false)
        assert(model.bestReposNoResult.value == false)
        assert(model.bestReposRetry.value == false)
    }

    @Test
    fun updateStatus_no_result() {
        val model = OrganizationRepositoriesViewModel()
        model.updateStatus(OrganizationRepositoriesViewModel.ResponseStatus.NO_RESULT)
        assert(model.bestReposLoading.value == false)
        assert(model.bestReposNoResult.value == true)
        assert(model.bestReposRetry.value == false)
    }

    @Test
    fun updateStatus_error_io() {
        val model = OrganizationRepositoriesViewModel()
        model.updateStatus(OrganizationRepositoriesViewModel.ResponseStatus.ERROR_IO)
        assert(model.bestReposLoading.value == false)
        assert(model.bestReposNoResult.value == false)
        assert(model.bestReposRetry.value == true)
    }

    @Test
    fun updateStatus_error_other() {
        val model = OrganizationRepositoriesViewModel()
        model.updateStatus(OrganizationRepositoriesViewModel.ResponseStatus.ERROR_OTHER)
        assert(model.bestReposLoading.value == false)
        assert(model.bestReposNoResult.value == false)
        assert(model.bestReposRetry.value == true)
    }

    @Test
    fun updateStatus_null() {
        val model = OrganizationRepositoriesViewModel()
        model.updateStatus(null)
        assert(model.bestReposLoading.value == false)
        assert(model.bestReposNoResult.value == false)
        assert(model.bestReposRetry.value == false)
    }
}