package com.example.mydiabetesapp.feature.notification.data

import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRepositoryTest {
    private lateinit var dao: NotificationDao
    private lateinit var repo: NotificationRepository

    @Before fun setup() {
        dao = mockk()
        repo = NotificationRepository(dao)
    }

    @Test
    fun `getNotifications returns flow from dao`() = runTest {
        val sample = listOf(
            NotificationEntry(
                message="Test", date="01.01.2025", time="08:00",
                repeatDaily=false, intervalMinutes=0,
                autoCancel=true, enabled=true, reminderType="test"
            )
        )
        val flow = MutableStateFlow(sample)
        every { dao.getAllNotifications() } returns flow

        val result = repo.getNotifications().first()

        assertEquals(sample, result)
        verify { dao.getAllNotifications() }
    }

    @Test
    fun `addNotification calls dao insertNotification`() = runTest {
        val entry = NotificationEntry(
            message="Msg", date="02.02.2025", time="09:00",
            repeatDaily=true, intervalMinutes=15,
            autoCancel=false, enabled=true, reminderType="test"
        )
        coEvery { dao.insertNotification(entry) } returns 5L

        val id = repo.addNotification(entry)

        assertEquals(5L, id)
        coVerify { dao.insertNotification(entry) }
    }

    @Test
    fun `updateNotification calls dao updateNotification`() = runTest {
        val entry = NotificationEntry(
            id=3, message="Msg", date="03.03.2025", time="10:00",
            repeatDaily=false, intervalMinutes=0,
            autoCancel=true, enabled=false, reminderType="test"
        )
        coEvery { dao.updateNotification(entry) } just Runs

        repo.updateNotification(entry)

        coVerify { dao.updateNotification(entry) }
    }

    @Test
    fun `deleteNotification calls dao deleteNotification`() = runTest {
        val entry = NotificationEntry(
            id=7, message="Msg", date="04.04.2025", time="11:00",
            repeatDaily=false, intervalMinutes=0,
            autoCancel=true, enabled=true, reminderType="test"
        )
        coEvery { dao.deleteNotification(7) } just Runs

        repo.deleteNotification(entry)

        coVerify { dao.deleteNotification(7) }
    }
}
