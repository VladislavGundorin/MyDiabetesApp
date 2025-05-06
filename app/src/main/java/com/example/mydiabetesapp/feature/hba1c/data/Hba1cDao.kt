package com.example.mydiabetesapp.feature.hba1c.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Hba1cDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: Hba1cEntry): Long

    @Update
    suspend fun update(entry: Hba1cEntry)

    @Delete
    suspend fun delete(entry: Hba1cEntry)

    @Query("SELECT * FROM hba1c_entries ORDER BY date DESC")
    fun getAll(): Flow<List<Hba1cEntry>>

    @Query("SELECT * FROM hba1c_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Hba1cEntry?

    @Query("DELETE FROM hba1c_entries WHERE id = :id")
    suspend fun deleteById(id: Int)
    @Query("SELECT * FROM hba1c_entries WHERE date BETWEEN :from AND :to")
    suspend fun getBetween(from: String, to: String): List<Hba1cEntry>

}
