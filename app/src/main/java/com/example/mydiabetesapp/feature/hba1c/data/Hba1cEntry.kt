package com.example.mydiabetesapp.feature.hba1c.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mydiabetesapp.feature.profile.data.UserProfile

@Entity(
    tableName = "hba1c_entries",
    foreignKeys = [ForeignKey(
        entity = UserProfile::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class Hba1cEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val date: String,
    val hba1c: Float
)
