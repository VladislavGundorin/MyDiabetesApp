package com.example.mydiabetesapp.feature.hba1c.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Hba1cViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var repository: Hba1cRepository
    private lateinit var viewModel: Hba1cViewModel

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After  fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `entries StateFlow emits list from repository`() = runTest {
        val sample = listOf(Hba1cEntry(userId = 1, date = "01.01.2025", hba1c = 5.6f))
        repository = mockk(relaxed = true)
        coEvery { repository.getEntries() } returns flowOf(sample)

        viewModel = Hba1cViewModel(repository)
        val job = launch { viewModel.entries.collect {} }
        dispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(sample, viewModel.entries.value)
        coVerify { repository.getEntries() }
    }

    @Test
    fun `add calls repository add`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = Hba1cViewModel(repository)
        val entry = Hba1cEntry(userId = 1, date = "02.02.2025", hba1c = 6.1f)

        viewModel.add(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.add(entry) }
    }

    @Test
    fun `update calls repository update`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = Hba1cViewModel(repository)
        val entry = Hba1cEntry(id = 42, userId = 1, date = "03.03.2025", hba1c = 7.2f)

        viewModel.update(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.update(entry) }
    }

    @Test
    fun `delete calls repository delete`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = Hba1cViewModel(repository)
        val entry = Hba1cEntry(id = 99, userId = 1, date = "04.04.2025", hba1c = 8.3f)

        viewModel.delete(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.delete(entry) }
    }
}
