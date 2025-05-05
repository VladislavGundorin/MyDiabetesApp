package com.example.mydiabetesapp.repository

import com.example.mydiabetesapp.data.database.BloodPressureDao
import com.example.mydiabetesapp.data.database.BloodPressureEntry
import kotlinx.coroutines.flow.Flow

class BloodPressureRepository(private val dao: BloodPressureDao) {

    suspend fun insert(entry: BloodPressureEntry) = dao.insert(entry)
    suspend fun update(entry: BloodPressureEntry) = dao.update(entry)
    suspend fun delete(entry: BloodPressureEntry) = dao.delete(entry)
    fun getAll(): Flow<List<BloodPressureEntry>> = dao.getAll()
}
