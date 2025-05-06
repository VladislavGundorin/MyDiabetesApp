package com.example.mydiabetesapp.feature.pressure.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mydiabetesapp.feature.profile.data.UserProfile

@Entity(
    tableName = "blood_pressure_entries",
    foreignKeys = [ForeignKey(
        entity = UserProfile::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class BloodPressureEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val date: String,
    val time: String,
    val systolic: Int,    // Систолическое давление
    val diastolic: Int,   // Диастолическое давление
    val pulse: Int
)
