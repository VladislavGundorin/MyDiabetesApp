package com.example.mydiabetesapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: GlucoseEntry)

    @Update
    suspend fun updateEntry(entry: GlucoseEntry)

    @Delete
    suspend fun deleteEntry(entry: GlucoseEntry)

    @Query("SELECT * FROM glucose_entries ORDER BY date DESC, time DESC")
    fun getAllEntries(): Flow<List<GlucoseEntry>>

    @Query("SELECT * FROM glucose_entries WHERE date BETWEEN :from AND :to")
    suspend fun getBetween(from: String, to: String): List<GlucoseEntry>
}