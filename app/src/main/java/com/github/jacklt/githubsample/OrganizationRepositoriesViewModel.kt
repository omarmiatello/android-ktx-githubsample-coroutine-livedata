package com.github.jacklt.githubsample

import androidx.lifecycle.MutableLiveData
import com.github.jacklt.githubsample.data.RepositoryItem
import com.github.jacklt.githubsample.utils.ScopedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class OrganizationRepositoriesViewModel : ScopedViewModel() {
    val query: MutableLiveData<String> = MutableLiveData()

    val bestReposList = MutableLiveData<List<RepositoryItem>>()
    val bestReposStatus = MutableLiveData<ResponseStatus>()

    val bestReposLoading = MutableLiveData<Boolean>()
    val bestReposNoResult = MutableLiveData<Boolean>()
    val bestReposRetry = MutableLiveData<Boolean>()

    init {
        launch {
            var job: Job? = null
            for (q in query.toChannel()) {
                services.logD("Query: $q")
                job?.cancel()
                job = launch {
                    bestReposList.value = emptyList()
                    if (q.isNotBlank()) {
                        delay(500)
                        bestReposList.value = getAllRepo(q.trim())
                            .sortedByDescending { it.stars }
                            .take(3)
                    } else {
                        bestReposStatus.value = null
                    }
                }
            }
        }

        launch {
            for (status in bestReposStatus.toChannel()) {
                updateStatus(status)
            }
        }
    }

    fun updateStatus(status: ResponseStatus?) {
        when (status) {
            ResponseStatus.LOADING -> {
                bestReposLoading.value = true
                bestReposNoResult.value = false
                bestReposRetry.value = false
            }
            ResponseStatus.COMPLETED -> {
                bestReposLoading.value = false
                bestReposNoResult.value = false
                bestReposRetry.value = false
            }
            ResponseStatus.NO_RESULT -> {
                bestReposLoading.value = false
                bestReposNoResult.value = true
                bestReposRetry.value = false
            }
            ResponseStatus.ERROR_IO,
            ResponseStatus.ERROR_OTHER -> {
                bestReposLoading.value = false
                bestReposNoResult.value = false
                bestReposRetry.value = true
            }
            null -> {
                bestReposLoading.value = false
                bestReposNoResult.value = false
                bestReposRetry.value = false
            }
        }
    }

    suspend fun getAllRepo(q: String): List<RepositoryItem> {
        val githubService = services.github
        bestReposStatus.value = ResponseStatus.LOADING
        try {
            val repos = ArrayList<RepositoryItem>()
            var page = 1
            var completed = false
            while (!completed) {
                val currentPage = githubService.organizationRepos(q, page).await().map { it.toAppItem() }
                repos += currentPage
                page++
                completed = currentPage.size != 30
            }
            bestReposStatus.value = if (repos.isEmpty()) ResponseStatus.NO_RESULT else ResponseStatus.COMPLETED
            return repos
        } catch (e: IOException) {
            bestReposStatus.value = ResponseStatus.ERROR_IO
        } catch (e: HttpException) {
            bestReposStatus.value = if (e.code() == 404) {
                ResponseStatus.NO_RESULT
            } else {
                ResponseStatus.ERROR_OTHER
            }
        }
        return emptyList()
    }

    enum class ResponseStatus {
        LOADING,
        COMPLETED,
        NO_RESULT,
        ERROR_IO,
        ERROR_OTHER
    }
}