package com.example.mydiabetesapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val date: String,
    val time: String,
    val repeatDaily: Boolean,
    val intervalMinutes: Long,
    val autoCancel: Boolean,
    val enabled: Boolean,
    val reminderType: String
)
