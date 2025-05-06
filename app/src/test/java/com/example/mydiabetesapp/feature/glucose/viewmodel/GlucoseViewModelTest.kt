package com.example.mydiabetesapp.feature.glucose.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mydiabetesapp.feature.glucose.data.GlucoseEntry
import com.example.mydiabetesapp.feature.glucose.data.GlucoseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GlucoseViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var repository: GlucoseRepository
    private lateinit var viewModel: GlucoseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entries StateFlow emits list from repository`() = runTest {
        val sampleList = listOf(
            GlucoseEntry(
                userId       = 1,
                date         = "01.01.2025",
                time         = "08:00",
                glucoseLevel = 5.5f,
                category     = "before"
            )
        )
        repository = mockk(relaxed = true)
        coEvery { repository.getAllEntries() } returns flowOf(sampleList)

        viewModel = GlucoseViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(sampleList, viewModel.entries.value)
        coVerify { repository.getAllEntries() }
    }

    @Test
    fun `addEntry calls repository insert`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = GlucoseViewModel(repository)

        val entry = GlucoseEntry(
            userId       = 1,
            date         = "02.02.2025",
            time         = "09:00",
            glucoseLevel = 7.2f,
            category     = "fasting"
        )

        viewModel.addEntry(entry)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insert(entry) }
    }
}
