package com.example.mydiabetesapp.feature.pressure.data

import kotlinx.coroutines.flow.Flow

class BloodPressureRepository(private val dao: BloodPressureDao) {

    suspend fun insert(entry: BloodPressureEntry) = dao.insert(entry)
    suspend fun update(entry: BloodPressureEntry) = dao.update(entry)
    suspend fun delete(entry: BloodPressureEntry) = dao.delete(entry)
    fun getAll(): Flow<List<BloodPressureEntry>> = dao.getAll()
}
