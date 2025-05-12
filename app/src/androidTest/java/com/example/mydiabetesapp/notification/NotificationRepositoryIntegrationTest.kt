package com.example.mydiabetesapp.notification

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.notification.data.NotificationDao
import com.example.mydiabetesapp.feature.notification.data.NotificationEntry
import com.example.mydiabetesapp.feature.notification.data.NotificationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: NotificationDao
    private lateinit var repo: NotificationRepository

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = db.notificationDao()
        repo = NotificationRepository(dao)
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun flowReflectsDb() = runBlocking {
        val entry = NotificationEntry(
            message="Z", date="05.05.2025", time="14:00",
            repeatDaily=false, intervalMinutes=0,
            autoCancel=true, enabled=true, reminderType="z"
        )
        dao.insertNotification(entry)

        val list = repo.getNotifications().first()
        Assert.assertEquals(1, list.size)
    }
}
