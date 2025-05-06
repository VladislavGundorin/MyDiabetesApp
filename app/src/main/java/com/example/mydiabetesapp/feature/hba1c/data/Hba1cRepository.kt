package com.example.mydiabetesapp.feature.hba1c.data

import kotlinx.coroutines.flow.Flow

class Hba1cRepository(private val dao: Hba1cDao) {

    fun getEntries(): Flow<List<Hba1cEntry>> = dao.getAll()

    suspend fun add(entry: Hba1cEntry) = dao.insert(entry)

    suspend fun update(entry: Hba1cEntry) = dao.update(entry)

    suspend fun delete(entry: Hba1cEntry) = dao.deleteById(entry.id)
    suspend fun get(id: Int) = dao.getById(id)

}
