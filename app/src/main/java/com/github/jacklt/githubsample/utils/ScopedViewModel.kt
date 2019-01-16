package com.github.jacklt.githubsample.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.github.jacklt.githubsample.services
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

open class ScopedViewModel : ViewModel(), CoroutineScope {
    private val job = Job()
    private val onClear = ArrayList<() -> Unit>()

    override val coroutineContext = Dispatchers.Main + job

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        onClear.forEach { it() }
        services.logD("ScopedViewModel.onCleared()")
    }

    fun <T> LiveData<T>.toChannel(): ReceiveChannel<T> = Channel<T>().also { channel ->
        Observer<T> {
            launch { channel.send(it) }
        }.also { observer ->
            services.logD("ScopedViewModel LiveData.toChannel(): Start observe $this")
            observeForever(observer)
            onClear += {
                services.logD("ScopedViewModel LiveData.toChannel(): Stop observe $this")
                channel.close()
                removeObserver(observer)
            }
        }
    }
}