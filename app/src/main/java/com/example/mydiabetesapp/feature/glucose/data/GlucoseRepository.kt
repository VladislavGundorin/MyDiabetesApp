package com.example.mydiabetesapp.feature.glucose.data

import kotlinx.coroutines.flow.Flow

class GlucoseRepository(private val glucoseDao: GlucoseDao) {

    suspend fun insert(entry: GlucoseEntry) {
        glucoseDao.insertEntry(entry)
    }

    suspend fun update(entry: GlucoseEntry) {
        glucoseDao.updateEntry(entry)
    }

    suspend fun delete(entry: GlucoseEntry) {
        glucoseDao.deleteEntry(entry)
    }

    fun getAllEntries(): Flow<List<GlucoseEntry>> {
        return glucoseDao.getAllEntries()
    }
}
