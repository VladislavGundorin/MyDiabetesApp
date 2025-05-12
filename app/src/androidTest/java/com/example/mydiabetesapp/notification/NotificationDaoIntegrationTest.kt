package com.example.mydiabetesapp.notification

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.notification.data.NotificationDao
import com.example.mydiabetesapp.feature.notification.data.NotificationEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationDaoIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: NotificationDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = db.notificationDao()
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun insertAndGetAll() = runBlocking {
        val n = NotificationEntry(
            message="Hi", date="01.01.2025", time="12:00",
            repeatDaily=true, intervalMinutes=30, autoCancel=false,
            enabled=true, reminderType="test"
        )
        dao.insertNotification(n)

        val list = dao.getAllNotifications().first()
        Assert.assertEquals(1, list.size)
    }

    @Test fun getByIdAndDelete() = runBlocking {
        val id = dao.insertNotification(
            NotificationEntry(message="X", date="02.02.2025", time="13:00",
                repeatDaily=false, intervalMinutes=0,
                autoCancel=true, enabled=true, reminderType="x")
        ).toInt()

        val fetched = dao.getNotificationById(id)
        Assert.assertNotNull(fetched)

        dao.deleteNotification(id)
        Assert.assertNull(dao.getNotificationById(id))
    }
}
