package com.example.mydiabetesapp.pressure

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureDao
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureEntry
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BloodPressureDaoIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BloodPressureDao

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
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun insertAndGetAll() = runBlocking {
        val e1 = BloodPressureEntry( userId=1, date="01.01.2025", time="07:15", systolic=120, diastolic=80, pulse=70)
        dao.insert(e1)

        val list = dao.getAll().first()
        Assert.assertEquals(1, list.size)
    }
}
