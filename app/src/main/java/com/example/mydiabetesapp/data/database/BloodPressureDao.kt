package com.example.mydiabetesapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BloodPressureEntry)

    @Update suspend fun update(entry: BloodPressureEntry)

    @Delete suspend fun delete(entry: BloodPressureEntry)

    @Query("SELECT * FROM blood_pressure_entries ORDER BY date DESC, time DESC")
    fun getAll(): Flow<List<BloodPressureEntry>>

    @Query("SELECT * FROM blood_pressure_entries WHERE date BETWEEN :from AND :to")
    suspend fun getBetween(from: String, to: String): List<BloodPressureEntry>
}
