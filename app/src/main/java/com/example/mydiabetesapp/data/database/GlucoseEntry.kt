package com.example.mydiabetesapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_entries")
data class GlucoseEntry (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val time: String,
    val glucoseLevel: Float,
    val insulineDose: Float?,
    val carbs: Float?
)