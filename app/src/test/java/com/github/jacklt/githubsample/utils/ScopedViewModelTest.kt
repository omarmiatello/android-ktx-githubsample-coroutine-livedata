package com.github.jacklt.githubsample.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.github.jacklt.githubsample.ServicesConfig
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ScopedViewModelTest {
    // https://github.com/Kotlin/kotlinx.coroutines/tree/master/core/kotlinx-coroutines-test
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        ServicesConfig.buildInstance(
            ServicesConfig(
                OkHttpClient(),
                logD = { println("Log: $it") })
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun onClear_cancelCoroutineScope() {
        object : ScopedViewModel() {
            init {
                assertTrue(coroutineContext.isActive)

                onCleared()

                assertFalse(coroutineContext.isActive)
            }
        }
    }

    @Test
    fun liveDataToChannel() {
        object : ScopedViewModel() {
            init {
                val liveData = MutableLiveData<Int>()
                val channel = liveData.toChannel()
                liveData.value = 2

                val valueFromChannel = runBlocking { channel.receive() }

                assertEquals(2, valueFromChannel)

                onCleared()
            }
        }
    }

    @Test
    fun liveDataToChannel_closeChannelOnClearResources() {
        object : ScopedViewModel() {
            init {
                val liveData = MutableLiveData<String>()
                val channel = liveData.toChannel()

                assertFalse(channel.isClosedForReceive)

                onCleared()

                assertTrue(channel.isClosedForReceive)
            }
        }
    }
}