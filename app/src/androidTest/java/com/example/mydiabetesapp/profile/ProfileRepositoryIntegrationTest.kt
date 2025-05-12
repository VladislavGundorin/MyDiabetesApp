package com.example.mydiabetesapp.profile

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.profile.data.ProfileRepository
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import com.example.mydiabetesapp.feature.profile.data.UserProfileDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserProfileDao
    private lateinit var repo: ProfileRepository

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = db.userProfileDao()
        repo = ProfileRepository(dao)
    }

    @After fun teardown() {
        db.close()
    }

    @Test fun getProfileInitiallyNull() = runBlocking {
        Assert.assertNull(repo.getProfile().first())
    }

    @Test fun insertAndGetProfile() = runBlocking {
        dao.insertUserProfile(UserProfile(id=1, name="C", gender="F", age=25, height=165f, weight=60f))
        val p = repo.getProfile().first()
        Assert.assertNotNull(p)
        Assert.assertEquals("C", p!!.name)
    }

    @Test fun updateProfile() = runBlocking {
        dao.insertUserProfile(UserProfile(id=1, name="D", gender="", age=0, height=0f, weight=0f))
        val updated = UserProfile(id=1, name="E", gender="", age=0, height=0f, weight=0f)
        repo.updateProfile(updated)
        val p = dao.getUserById(1)!!
        Assert.assertEquals("E", p.name)
    }
}
