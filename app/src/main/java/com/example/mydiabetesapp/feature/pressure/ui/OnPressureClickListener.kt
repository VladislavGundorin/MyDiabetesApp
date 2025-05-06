package com.example.mydiabetesapp.feature.pressure.ui

import com.example.mydiabetesapp.feature.pressure.data.BloodPressureEntry

interface OnPressureClickListener {
    fun onEdit(e: BloodPressureEntry)
    fun onDelete(e: BloodPressureEntry)
}