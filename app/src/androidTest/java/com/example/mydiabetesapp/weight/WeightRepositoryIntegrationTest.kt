package com.example.mydiabetesapp.weight

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.weight.data.WeightDao
import com.example.mydiabetesapp.feature.weight.data.WeightEntry
import com.example.mydiabetesapp.feature.weight.data.WeightRepository
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeightRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WeightDao
    private lateinit var repo: WeightRepository

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
        repo = WeightRepository(dao)
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun flowReflectsDb() = runBlocking {
        val entry = WeightEntry(userId=1, date="03.03.2025", time="09:30", weight=72f)
        dao.insertWeightEntry(entry)

        val items = repo.getWeightEntries().first()
        Assert.assertEquals(1, items.size)
        Assert.assertEquals(72f, items[0].weight, 0.001f)
    }
}
