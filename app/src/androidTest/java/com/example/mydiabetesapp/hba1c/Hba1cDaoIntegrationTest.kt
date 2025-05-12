package com.example.mydiabetesapp.hba1c

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cDao
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Hba1cDaoIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: Hba1cDao

    @Before fun setup() {
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

        dao = db.hba1cDao()
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun insertAndGetAll() = runBlocking {
        val entry = Hba1cEntry(userId = 1, date = "05.05.2025", hba1c = 6.2f)
        dao.insert(entry)

        val list = dao.getAll().first()
        Assert.assertEquals(1, list.size)
        Assert.assertEquals(6.2f, list[0].hba1c, 0.001f)
    }
}
