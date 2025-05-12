package com.example.mydiabetesapp.weight

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.weight.data.WeightDao
import com.example.mydiabetesapp.feature.weight.data.WeightEntry
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeightDaoIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WeightDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        runBlocking {
            db.userProfileDao().insertUserProfile(
                UserProfile(1, "Test", "", 0, 0f, 0f)
            )
        }
        dao = db.weightDao()
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun insertAndGetAll() = runBlocking {
        val e1 = WeightEntry(userId = 1, date="01.01.2025", time="07:00", weight=70f)
        val e2 = WeightEntry(userId = 1, date="02.01.2025", time="08:00", weight=71f)
        dao.insertWeightEntry(e1)
        dao.insertWeightEntry(e2)

        val list = dao.getAllWeightEntries().first()
        Assert.assertEquals(2, list.size)
    }

    @Test fun getBetweenDates() = runBlocking {
        val e1 = WeightEntry(userId = 1, date="01.01.2025", time="07:00", weight=70f)
        val e2 = WeightEntry(userId = 1, date="02.01.2025", time="08:00", weight=71f)
        dao.insertWeightEntry(e1)
        dao.insertWeightEntry(e2)

        val between = dao.getBetween("01.01.2025", "01.01.2025")
        Assert.assertEquals(1, between.size)
        Assert.assertEquals(70f, between[0].weight, 0.001f)
    }
}
