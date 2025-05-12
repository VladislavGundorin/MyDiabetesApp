package com.example.mydiabetesapp.feature.profile.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mydiabetesapp.feature.profile.data.ProfileRepository
import com.example.mydiabetesapp.feature.profile.data.UserProfile
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
class ProfileViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var repository: ProfileRepository
    private lateinit var viewModel: ProfileViewModel

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After  fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `profile StateFlow emits data from repository`() = runTest {
        val sample = UserProfile(id=1, name="A", gender="M", age=30, height=180f, weight=75f)
        repository = mockk(relaxed = true)
        coEvery { repository.getProfile() } returns flowOf(sample)

        viewModel = ProfileViewModel(repository)
        val job = launch { viewModel.profile.collect {} }
        dispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(sample, viewModel.profile.value)
        coVerify { repository.getProfile() }
    }

    @Test
    fun `updateProfile calls repository update`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = ProfileViewModel(repository)
        val prof = UserProfile(id=1, name="B", gender="F", age=25, height=165f, weight=60f)

        viewModel.updateProfile(prof)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { repository.updateProfile(prof) }
    }

    @Test
    fun `insertProfile calls repository insert`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = ProfileViewModel(repository)
        val prof = UserProfile(id=0, name="C", gender="M", age=40, height=170f, weight=80f)

        viewModel.insertProfile(prof)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { repository.insertProfile(prof) }
    }
}
