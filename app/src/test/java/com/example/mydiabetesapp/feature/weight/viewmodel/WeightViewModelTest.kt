package com.example.mydiabetesapp.feature.weight.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mydiabetesapp.feature.weight.data.WeightEntry
import com.example.mydiabetesapp.feature.weight.data.WeightRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeightViewModelTest {
    @get:Rule val instantExecutor = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: WeightRepository
    private lateinit var vm: WeightViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }
    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entries emits data from repo`() = runTest {
        val sample = listOf(
            WeightEntry(
                userId = 1,
                date   = "01.01.2025",
                time   = "08:00",
                weight = 70f
            )
        )
        repo = mockk(relaxed = true)
        coEvery { repo.getWeightEntries() } returns flowOf(sample)

        vm = WeightViewModel(repo)

        val job = launch { vm.entries.collect {} }
        dispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(sample, vm.entries.value)
        coVerify { repo.getWeightEntries() }
    }

    @Test
    fun `addEntry calls repo add`() = runTest {
        repo = mockk(relaxed = true)
        vm = WeightViewModel(repo)
        val entry = WeightEntry(userId=1, date="02.02.2025", time="09:00", weight=71f)

        vm.addEntry(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.addWeightEntry(entry) }
    }

    @Test
    fun `updateEntry calls repo update`() = runTest {
        repo = mockk(relaxed = true)
        vm = WeightViewModel(repo)
        val entry = WeightEntry(id=2, userId=1, date="03.03.2025", time="10:00", weight=72f)

        vm.updateEntry(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.updateWeightEntry(entry) }
    }

    @Test
    fun `deleteEntry calls repo delete`() = runTest {
        repo = mockk(relaxed = true)
        vm = WeightViewModel(repo)
        val entry = WeightEntry(id=3, userId=1, date="04.04.2025", time="11:00", weight=73f)

        vm.deleteEntry(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.deleteWeightEntry(entry) }
    }
}
