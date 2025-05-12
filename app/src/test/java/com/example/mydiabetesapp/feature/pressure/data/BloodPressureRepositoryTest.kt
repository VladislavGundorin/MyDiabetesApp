package com.example.mydiabetesapp.feature.pressure.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalCoroutinesApi::class)
class BloodPressureRepositoryTest {
    private lateinit var dao: BloodPressureDao
    private lateinit var repo: BloodPressureRepository

    @Before fun setup() {
        dao = mockk()
        repo = BloodPressureRepository(dao)
    }

    @Test
    fun `getAll returns flow from dao`() = runTest {
        val sample = listOf(
            BloodPressureEntry(userId=1, date="01.01.2025", time="08:00", systolic=120, diastolic=80, pulse=70)
        )
        val flow = MutableStateFlow(sample)
        coEvery { dao.getAll() } returns flow

        val result = repo.getAll().first()

        assertEquals(sample, result)
        coVerify { dao.getAll() }
    }

    @Test
    fun `insert calls dao insert`() = runTest {
        val entry = BloodPressureEntry(userId=1, date="02.02.2025", time="09:00", systolic=130, diastolic=85, pulse=75)
        coEvery { dao.insert(entry) } returns Unit

        repo.insert(entry)

        coVerify { dao.insert(entry) }
    }

    @Test
    fun `update calls dao update`() = runTest {
        val entry = BloodPressureEntry(id=2, userId=1, date="03.03.2025", time="10:00", systolic=125, diastolic=82, pulse=72)
        coEvery { dao.update(entry) } returns Unit

        repo.update(entry)

        coVerify { dao.update(entry) }
    }

    @Test
    fun `delete calls dao delete`() = runTest {
        val entry = BloodPressureEntry(id=3, userId=1, date="04.04.2025", time="11:00", systolic=118, diastolic=78, pulse=68)
        coEvery { dao.delete(entry) } returns Unit

        repo.delete(entry)

        coVerify { dao.delete(entry) }
    }
}
