package com.example.mydiabetesapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntry): Long

    @Update
    suspend fun updateWeightEntry(entry: WeightEntry)

    @Query("SELECT * FROM weight_entries WHERE id = :id LIMIT 1")
    suspend fun getWeightEntryById(id: Int): WeightEntry?

    @Query("SELECT * FROM weight_entries ORDER BY date, time")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteWeightEntry(id: Int)

    @Query("SELECT * FROM weight_entries WHERE date BETWEEN :from AND :to")
    suspend fun getBetween(from: String, to: String): List<WeightEntry>
}
