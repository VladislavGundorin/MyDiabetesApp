package com.example.mydiabetesapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert               suspend fun insert(report: ReportEntry): Long
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReportEntry>>
    @Delete               suspend fun delete(report: ReportEntry)
}
