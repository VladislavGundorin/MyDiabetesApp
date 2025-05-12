package com.example.mydiabetesapp.profile

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import com.example.mydiabetesapp.feature.profile.data.UserProfileDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserProfileDaoIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserProfileDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = db.userProfileDao()
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun insertAndGetById() = runBlocking {
        val id = dao.insertUserProfile(UserProfile(name="Bob", gender="M", age=30, height=180f, weight=75f)).toInt()
        val p = dao.getUserById(id)
        Assert.assertNotNull(p)
        Assert.assertEquals("Bob", p!!.name)
    }

    @Test fun getAllAndCount() = runBlocking {
        dao.insertUserProfile(UserProfile(name="A", gender="", age=0, height=0f, weight=0f))
        dao.insertUserProfile(UserProfile(name="B", gender="", age=0, height=0f, weight=0f))
        val list = dao.getAllUserProfiles().first()
        Assert.assertTrue(list.size >= 2)
        Assert.assertTrue(dao.getCount() >= 2)
    }
}
