package com.example.mydiabetesapp.feature.pressure.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureEntry
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureRepository
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
class BloodPressureViewModelTest {
    @get:Rule val instantExecutor = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: BloodPressureRepository
    private lateinit var vm: BloodPressureViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }
    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entries emits data from repo`() = runTest {
        val sample = listOf(BloodPressureEntry(userId=1, date="01.01.2025", time="08:00", systolic=120, diastolic=80, pulse=70))
        repo = mockk(relaxed = true)
        coEvery { repo.getAll() } returns flowOf(sample)

        vm = BloodPressureViewModel(repo)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(sample, vm.entries.value)
        coVerify { repo.getAll() }
    }

    @Test
    fun `insert calls repo insert`() = runTest {
        repo = mockk(relaxed = true)
        vm = BloodPressureViewModel(repo)
        val entry = BloodPressureEntry(userId=1, date="02.02.2025", time="09:00", systolic=130, diastolic=85, pulse=75)

        vm.insert(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.insert(entry) }
    }

    @Test
    fun `update calls repo update`() = runTest {
        repo = mockk(relaxed = true)
        vm = BloodPressureViewModel(repo)
        val entry = BloodPressureEntry(id=2, userId=1, date="03.03.2025", time="10:00", systolic=125, diastolic=82, pulse=72)

        vm.update(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.update(entry) }
    }

    @Test
    fun `delete calls repo delete`() = runTest {
        repo = mockk(relaxed = true)
        vm = BloodPressureViewModel(repo)
        val entry = BloodPressureEntry(id=3, userId=1, date="04.04.2025", time="11:00", systolic=118, diastolic=78, pulse=68)

        vm.delete(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.delete(entry) }
    }
}
