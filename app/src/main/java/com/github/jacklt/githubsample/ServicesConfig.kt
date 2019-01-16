package com.github.jacklt.githubsample

import android.util.Log
import com.github.jacklt.githubsample.data.GitHubService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

class ServicesConfig(
    val okHttpClient: OkHttpClient = OkHttpClient.Builder().apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        }
    }.build(),

    val github: GitHubService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(okHttpClient)
        .build()
        .create(GitHubService::class.java),

    val logD: (String) -> Unit = { Log.d("GithubSample", it) }
) {
    companion object {
        var instance by Delegates.notNull<ServicesConfig>()
        fun buildInstance(config: ServicesConfig = ServicesConfig()) {
            instance = config
        }
    }
}

val services get() = ServicesConfig.instance