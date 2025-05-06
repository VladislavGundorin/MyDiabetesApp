package com.example.mydiabetesapp.feature.weight.data

import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightDao: WeightDao) {

    fun getWeightEntries(): Flow<List<WeightEntry>> = weightDao.getAllWeightEntries()

    suspend fun addWeightEntry(entry: WeightEntry): Long {
        return weightDao.insertWeightEntry(entry)
    }

    suspend fun updateWeightEntry(entry: WeightEntry) {
        weightDao.updateWeightEntry(entry)
    }

    suspend fun deleteWeightEntry(entry: WeightEntry) {
        weightDao.deleteWeightEntry(entry.id)
    }
}
