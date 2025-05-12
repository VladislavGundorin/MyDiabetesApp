package com.example.mydiabetesapp.feature.profile.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.coVerify

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryTest {
    private lateinit var dao: UserProfileDao
    private lateinit var repo: ProfileRepository

    @Before fun setup() {
        dao = mockk()
        repo = ProfileRepository(dao)
    }

    @Test
    fun `getProfile maps first profile`() = runTest {
        val sampleList = listOf(UserProfile(id=1, name="A", gender="M", age=30, height=180f, weight=75f))
        coEvery { dao.getAllUserProfiles() } returns MutableStateFlow(sampleList)

        val result = repo.getProfile().first()

        assertEquals(sampleList[0], result)
        coVerify { dao.getAllUserProfiles() }
    }

    @Test
    fun `updateProfile calls dao updateUserProfile`() = runTest {
        val prof = UserProfile(id=1, name="B", gender="F", age=25, height=165f, weight=60f)
        coEvery { dao.updateUserProfile(prof) } returns Unit

        repo.updateProfile(prof)

        coVerify { dao.updateUserProfile(prof) }
    }

    @Test
    fun `insertProfile calls dao insertUserProfile`() = runTest {
        val prof = UserProfile(id=0, name="C", gender="M", age=40, height=170f, weight=80f)
        coEvery { dao.insertUserProfile(prof) } returns 10L

        val id = repo.insertProfile(prof)

        assertEquals(10L, id)
        coVerify { dao.insertUserProfile(prof) }
    }
}
