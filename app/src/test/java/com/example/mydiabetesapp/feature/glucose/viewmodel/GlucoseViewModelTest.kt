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
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GlucoseViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()

    private lateinit var repository: GlucoseRepository
    private lateinit var viewModel: GlucoseViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun `entries StateFlow emits list from repository`() = runTest {
        val sample = listOf(GlucoseEntry(userId = 1,
            date = "01.01.2025",
            time = "08:00",
            glucoseLevel = 5.5f,
            category = "before"))
        repository = mockk(relaxed = true)
        coEvery { repository.getAllEntries() } returns flowOf(sample)

        viewModel = GlucoseViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(sample, viewModel.entries.value)
        coVerify { repository.getAllEntries() }
    }

    @Test fun `addEntry calls repository insert`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = GlucoseViewModel(repository)
        val entry = GlucoseEntry(userId = 1, date = "02.02.2025", time = "09:00", glucoseLevel = 7.2f, category = "fasting")

        viewModel.addEntry(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insert(entry) }
    }

    @Test fun `updateEntry calls repository update`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = GlucoseViewModel(repository)
        val entry = GlucoseEntry(userId = 1, date = "03.03.2025", time = "10:00", glucoseLevel = 7.8f, category = "post-meal")

        viewModel.updateEntry(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.update(entry) }
    }

    @Test fun `deleteEntry calls repository delete`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = GlucoseViewModel(repository)
        val entry = GlucoseEntry(userId = 1, date = "04.04.2025", time = "11:00", glucoseLevel = 6.4f, category = "random")

        viewModel.deleteEntry(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.delete(entry) }
    }
}
