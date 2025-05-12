package com.example.mydiabetesapp.feature.glucose.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class GlucoseRepositoryTest {

    private lateinit var dao: GlucoseDao
    private lateinit var repository: GlucoseRepository

    @Before fun setup() {
        dao = mock(GlucoseDao::class.java)
        repository = GlucoseRepository(dao)
    }

    @Test fun `getAllEntries returns flow from dao`() = runTest {
        val sample = listOf(
            GlucoseEntry(userId = 1, date = "01.01.2025", time = "08:00", glucoseLevel = 5.5f, category = "before"),
            GlucoseEntry(userId = 1, date = "01.01.2025", time = "20:00", glucoseLevel = 6.0f, category = "after")
        )
        val flow = MutableStateFlow(sample)
        `when`(dao.getAllEntries()).thenReturn(flow)

        val result = repository.getAllEntries().first()

        assertEquals(sample, result)
        verify(dao).getAllEntries()
    }

    @Test fun `insert calls dao insertEntry`() = runTest {
        val entry = GlucoseEntry(userId = 1, date = "02.02.2025", time = "09:00", glucoseLevel = 7.2f, category = "fasting")
        repository.insert(entry)
        verify(dao).insertEntry(entry)
    }

    @Test fun `update calls dao updateEntry`() = runTest {
        val entry = GlucoseEntry(userId = 1, date = "03.03.2025", time = "10:00", glucoseLevel = 7.8f, category = "post-meal")
        repository.update(entry)
        verify(dao).updateEntry(entry)
    }

    @Test fun `delete calls dao deleteEntry`() = runTest {
        val entry = GlucoseEntry(userId = 1, date = "04.04.2025", time = "11:00", glucoseLevel = 6.4f, category = "random")
        repository.delete(entry)
        verify(dao).deleteEntry(entry)
    }
}
