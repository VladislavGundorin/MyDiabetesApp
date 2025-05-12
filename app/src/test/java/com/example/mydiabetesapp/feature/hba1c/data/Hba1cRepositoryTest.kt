package com.example.mydiabetesapp.feature.hba1c.data

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
class Hba1cRepositoryTest {
    private lateinit var dao: Hba1cDao
    private lateinit var repo: Hba1cRepository

    @Before fun setup() {
        dao = mockk()
        repo = Hba1cRepository(dao)
    }

    @Test
    fun `getEntries returns flow from dao`() = runTest {
        val sample = listOf(
            Hba1cEntry(userId=1, date="01.01.2025", hba1c=5.6f)
        )
        val flow = MutableStateFlow(sample)
        coEvery { dao.getAll() } returns flow

        val result = repo.getEntries().first()

        assertEquals(sample, result)
        coVerify { dao.getAll() }
    }

    @Test
    fun `add calls dao insert`() = runTest {
        val entry = Hba1cEntry(userId=1, date="02.02.2025", hba1c=6.1f)
        coEvery { dao.insert(entry) } returns 1L

        repo.add(entry)

        coVerify { dao.insert(entry) }
    }

    @Test
    fun `update calls dao update`() = runTest {
        val entry = Hba1cEntry(id=42, userId=1, date="03.03.2025", hba1c=7.2f)
        coEvery { dao.update(entry) } returns Unit

        repo.update(entry)

        coVerify { dao.update(entry) }
    }

    @Test
    fun `delete calls dao deleteById`() = runTest {
        val entry = Hba1cEntry(id=99, userId=1, date="04.04.2025", hba1c=8.3f)
        coEvery { dao.deleteById(99) } returns Unit

        repo.delete(entry)

        coVerify { dao.deleteById(99) }
    }

    @Test
    fun `get calls dao getById`() = runTest {
        val entry = Hba1cEntry(id=7, userId=1, date="05.05.2025", hba1c=5.5f)
        coEvery { dao.getById(7) } returns entry

        val result = repo.get(7)

        assertEquals(entry, result)
        coVerify { dao.getById(7) }
    }
}
