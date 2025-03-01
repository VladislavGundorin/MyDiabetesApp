package com.example.mydiabetesapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile): Long

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserProfile?

    @Query("SELECT * FROM user_profile")
    fun getAllUserProfiles(): Flow<List<UserProfile>>

    @Query("DELETE FROM user_profile WHERE id = :id")
    suspend fun deleteUserProfile(id: Int)
}
