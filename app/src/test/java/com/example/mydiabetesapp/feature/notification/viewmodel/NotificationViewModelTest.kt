package com.example.mydiabetesapp.feature.notification.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mydiabetesapp.feature.notification.data.NotificationEntry
import com.example.mydiabetesapp.feature.notification.data.NotificationRepository
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
class NotificationViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var repository: NotificationRepository
    private lateinit var viewModel: NotificationViewModel

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After  fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `notifications StateFlow emits list from repository`() = runTest {
        val sample = listOf(
            NotificationEntry(message="Test", date="01.01.2025", time="08:00",
                repeatDaily=false, intervalMinutes=0,
                autoCancel=true, enabled=true, reminderType="test")
        )
        repository = mockk(relaxed = true)
        coEvery { repository.getNotifications() } returns flowOf(sample)

        viewModel = NotificationViewModel(repository)
        val job = launch { viewModel.notifications.collect {} }
        dispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(sample, viewModel.notifications.value)
        coVerify { repository.getNotifications() }
    }

    @Test
    fun `addNotificationAndReturnId returns id from repository`() = runTest {
        repository = mockk()
        viewModel = NotificationViewModel(repository)
        val entry = NotificationEntry(message="X", date="02.02.2025", time="09:00",
            repeatDaily=false, intervalMinutes=0,
            autoCancel=true, enabled=true, reminderType="x")
        coEvery { repository.addNotification(entry) } returns 42L

        val id = viewModel.addNotificationAndReturnId(entry)
        assertEquals(42L, id)
        coVerify { repository.addNotification(entry) }
    }

    @Test
    fun `updateNotification calls repository update`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = NotificationViewModel(repository)
        val entry = NotificationEntry(id=5, message="X", date="03.03.2025", time="10:00",
            repeatDaily=false, intervalMinutes=0,
            autoCancel=true, enabled=true, reminderType="x")

        viewModel.updateNotification(entry)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { repository.updateNotification(entry) }
    }

    @Test
    fun `deleteNotification calls repository delete`() = runTest {
        repository = mockk(relaxed = true)
        viewModel = NotificationViewModel(repository)
        val entry = NotificationEntry(id=6, message="X", date="04.04.2025", time="11:00",
            repeatDaily=false, intervalMinutes=0,
            autoCancel=true, enabled=true, reminderType="x")

        viewModel.deleteNotification(entry)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { repository.deleteNotification(entry) }
    }
}
