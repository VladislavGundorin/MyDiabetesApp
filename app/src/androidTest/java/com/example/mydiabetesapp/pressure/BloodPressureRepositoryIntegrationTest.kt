package com.example.mydiabetesapp.pressure

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureDao
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureEntry
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureRepository
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BloodPressureRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BloodPressureDao
    private lateinit var repo: BloodPressureRepository

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
        dao = db.bloodPressureDao()
        repo = BloodPressureRepository(dao)
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun flowReflectsDb() = runBlocking {
        val entry = BloodPressureEntry( userId=1, date="03.03.2025", time="08:20", systolic=115, diastolic=75, pulse=68)
        dao.insert(entry)

        val items = repo.getAll().first()
        Assert.assertEquals(1, items.size)
        Assert.assertEquals(115, items[0].systolic)
    }
}
