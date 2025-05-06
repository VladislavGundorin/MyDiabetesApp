package com.example.mydiabetesapp.feature.notification.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(entry: NotificationEntry): Long

    @Update
    suspend fun updateNotification(entry: NotificationEntry)

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun getNotificationById(id: Int): NotificationEntry?

    @Query("SELECT * FROM notifications ORDER BY date, time")
    fun getAllNotifications(): Flow<List<NotificationEntry>>

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Int)
}
