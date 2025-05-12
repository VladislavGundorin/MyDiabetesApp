package com.example.mydiabetesapp.feature.notification.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mydiabetesapp.feature.notification.data.NotificationEntry
import com.example.mydiabetesapp.feature.notification.data.NotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `notifications StateFlow emits list from repository`() = runTest {
        val sample = listOf(
            NotificationEntry(
                id             = 0,
                message        = "Test",
                date           = "01.01.2025",
                time           = "08:00",
                repeatDaily    = false,
                intervalMinutes= 0,
                autoCancel     = true,
                enabled        = true,
                reminderType   = "test"
            )
        )

        val repository = mockk<NotificationRepository>(relaxed = true).apply {
            every { getNotifications() } returns flowOf(sample)
        }
        val viewModel = NotificationViewModel(repository)

        val job = launch { viewModel.notifications.collect {} }
        dispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(sample, viewModel.notifications.value)
        verify(exactly = 1) { repository.getNotifications() }
    }

    @Test
    fun `addNotificationAndReturnId returns id from repository`() = runTest {
        val entry = NotificationEntry(
            id             = 0,
            message        = "X",
            date           = "02.02.2025",
            time           = "09:00",
            repeatDaily    = false,
            intervalMinutes= 0,
            autoCancel     = true,
            enabled        = true,
            reminderType   = "x"
        )
        val repository = mockk<NotificationRepository>()
        coEvery { repository.addNotification(entry) } returns 42L
        every { repository.getNotifications() } returns MutableStateFlow(emptyList())

        val viewModel = NotificationViewModel(repository)

        val id = viewModel.addNotificationAndReturnId(entry)
        assertEquals(42L, id)
        coVerify(exactly = 1) { repository.addNotification(entry) }
    }

    @Test
    fun `updateNotification calls repository update`() = runTest {
        val entry = NotificationEntry(
            id             = 5,
            message        = "X",
            date           = "03.03.2025",
            time           = "10:00",
            repeatDaily    = false,
            intervalMinutes= 0,
            autoCancel     = true,
            enabled        = true,
            reminderType   = "x"
        )
        val repository = mockk<NotificationRepository>(relaxed = true).apply {
            every { getNotifications() } returns MutableStateFlow(emptyList())
            coEvery { updateNotification(entry) } returns Unit
        }
        val viewModel = NotificationViewModel(repository)

        viewModel.updateNotification(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateNotification(entry) }
    }

    @Test
    fun `deleteNotification calls repository delete`() = runTest {
        val entry = NotificationEntry(
            id             = 6,
            message        = "X",
            date           = "04.04.2025",
            time           = "11:00",
            repeatDaily    = false,
            intervalMinutes= 0,
            autoCancel     = true,
            enabled        = true,
            reminderType   = "x"
        )
        val repository = mockk<NotificationRepository>(relaxed = true).apply {
            every { getNotifications() } returns MutableStateFlow(emptyList())
            coEvery { deleteNotification(entry) } returns Unit
        }
        val viewModel = NotificationViewModel(repository)

        viewModel.deleteNotification(entry)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteNotification(entry) }
    }
}
