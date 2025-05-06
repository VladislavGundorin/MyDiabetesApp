package com.example.mydiabetesapp.feature.export.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName  : String,
    val uri       : String,
    val startDate : String,
    val endDate   : String,
    val daysCount : Int,
    val createdAt : Long = System.currentTimeMillis()
)
