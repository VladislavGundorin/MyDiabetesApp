package com.example.mydiabetesapp.feature.weight.data

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
class WeightRepositoryTest {
    private lateinit var dao: WeightDao
    private lateinit var repo: WeightRepository

    @Before fun setup() {
        dao = mockk()
        repo = WeightRepository(dao)
    }

    @Test
    fun `getWeightEntries returns flow from dao`() = runTest {
        val sample = listOf(WeightEntry(userId=1, date="01.01.2025", time="08:00", weight=70f))
        val flow = MutableStateFlow(sample)
        coEvery { dao.getAllWeightEntries() } returns flow

        val result = repo.getWeightEntries().first()

        assertEquals(sample, result)
        coVerify { dao.getAllWeightEntries() }
    }

    @Test
    fun `addWeightEntry calls dao insert`() = runTest {
        val entry = WeightEntry(userId=1, date="02.02.2025", time="09:00", weight=71f)
        coEvery { dao.insertWeightEntry(entry) } returns 1L

        repo.addWeightEntry(entry)

        coVerify { dao.insertWeightEntry(entry) }
    }

    @Test
    fun `updateWeightEntry calls dao update`() = runTest {
        val entry = WeightEntry(id=2, userId=1, date="03.03.2025", time="10:00", weight=72f)
        coEvery { dao.updateWeightEntry(entry) } returns Unit

        repo.updateWeightEntry(entry)

        coVerify { dao.updateWeightEntry(entry) }
    }

    @Test
    fun `deleteWeightEntry calls dao delete`() = runTest {
        val entry = WeightEntry(id=3, userId=1, date="04.04.2025", time="11:00", weight=73f)
        coEvery { dao.deleteWeightEntry(3) } returns Unit

        repo.deleteWeightEntry(entry)

        coVerify { dao.deleteWeightEntry(3) }
    }
}
