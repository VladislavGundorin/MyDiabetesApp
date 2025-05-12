package com.example.mydiabetesapp.hba1c

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cDao
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cRepository
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Hba1cRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: Hba1cDao
    private lateinit var repo: Hba1cRepository

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
        repo = Hba1cRepository(dao)
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun flowReflectsDb() = runBlocking {
        val entry = Hba1cEntry(userId = 1, date = "02.02.2025", hba1c = 5.9f)
        dao.insert(entry)

        val list = repo.getEntries().first()

        Assert.assertEquals(1, list.size)
        Assert.assertEquals(5.9f, list[0].hba1c, 0.001f)
    }
}
