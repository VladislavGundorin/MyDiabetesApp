package com.example.mydiabetesapp.feature.sync.viewmodel

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mydiabetesapp.feature.glucose.data.GlucoseEntry
import com.example.mydiabetesapp.feature.glucose.data.GlucoseRepository
import com.example.mydiabetesapp.feature.sync.ui.SyncFragment
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {
    @get:Rule val instantExecutor = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: GlucoseRepository
    private lateinit var vm: SyncViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        repo = mockk(relaxed = true)
        vm = SyncViewModel(repo)
    }
    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveMeasurement calls repo insert`() = runTest {
        val m = SyncFragment.Measurement(date="01.01.2025", time="08:00", value=5.5f, context="ctx")

        vm.saveMeasurement(m)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repo.insert(
                GlucoseEntry(userId=1, date="01.01.2025", time="08:00", glucoseLevel=5.5f, category="ctx")
            )
        }
    }
}
