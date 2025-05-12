package com.example.mydiabetesapp.glucose

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.glucose.data.GlucoseDao
import com.example.mydiabetesapp.feature.glucose.data.GlucoseEntry
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlucoseDaoIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: GlucoseDao

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
                UserProfile(
                    id     = 1,
                    name   = "Test",
                    gender = "",
                    age    = 0,
                    height = 0f,
                    weight = 0f
                )
            )
        }

        dao = db.glucoseDao()
    }


    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetAllEntries() = runBlocking {
        val entry = GlucoseEntry(
            userId       = 1,
            date         = "01.01.2025",
            time         = "08:00",
            glucoseLevel = 5.5f,
            category     = "тест"
        )
        dao.insertEntry(entry)

        val list = dao.getAllEntries().first()

        Assert.assertEquals(1, list.size)
        val retrieved = list[0]
        Assert.assertEquals(entry.userId,       retrieved.userId)
        Assert.assertEquals(entry.date,         retrieved.date)
        Assert.assertEquals(entry.time,         retrieved.time)
        Assert.assertEquals(entry.glucoseLevel, retrieved.glucoseLevel, 0.001f)
        Assert.assertEquals(entry.category,     retrieved.category)
    }

    @Test
    fun getBetweenDates() = runBlocking {
        val e1 = GlucoseEntry(userId=1, date="01.01.2025", time="08:00", glucoseLevel=5f, category="")
        val e2 = GlucoseEntry(userId=1, date="02.01.2025", time="09:00", glucoseLevel=6f, category="")
        dao.insertEntry(e1)
        dao.insertEntry(e2)

        val between = dao.getBetween("01.01.2025", "01.01.2025")
        Assert.assertEquals(1, between.size)
        val b = between[0]
        Assert.assertEquals("01.01.2025", b.date)
        Assert.assertEquals(5f, b.glucoseLevel, 0.001f)
    }
}
