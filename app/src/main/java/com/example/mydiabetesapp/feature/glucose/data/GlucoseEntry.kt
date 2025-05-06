package com.example.mydiabetesapp.feature.glucose.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mydiabetesapp.feature.profile.data.UserProfile

@Entity(
    tableName = "glucose_entries",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId","date","time"], unique = true)
    ]
)
data class GlucoseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val date: String,
    val time: String,
    val glucoseLevel: Float,
    val category: String = ""
)
