package com.example.mydiabetesapp.glucose

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.glucose.data.GlucoseDao
import com.example.mydiabetesapp.feature.glucose.data.GlucoseEntry
import com.example.mydiabetesapp.feature.glucose.data.GlucoseRepository
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlucoseRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: GlucoseDao
    private lateinit var repository: GlucoseRepository

    @Before
    fun setup() {
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

        dao = db.glucoseDao()
        repository = GlucoseRepository(dao)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun repositoryFlowReflectsDbEntries() = runBlocking {
        val entry = GlucoseEntry(
            userId       = 1,
            date         = "03.03.2025",
            time         = "10:00",
            glucoseLevel = 7.0f,
            category     = "интегр"
        )
        dao.insertEntry(entry)

        val items = repository.getAllEntries().first()
        Assert.assertEquals(1, items.size)
        val r = items[0]
        Assert.assertEquals(entry.userId,       r.userId)
        Assert.assertEquals(entry.date,         r.date)
        Assert.assertEquals(entry.time,         r.time)
        Assert.assertEquals(entry.glucoseLevel, r.glucoseLevel, 0.001f)
        Assert.assertEquals(entry.category,     r.category)
    }
}
