package com.example.mydiabetesapp.feature.glucose.ui

import com.example.mydiabetesapp.feature.glucose.data.GlucoseEntry

interface OnGlucoseEntryClickListener {
    fun onEdit(entry: GlucoseEntry)
    fun onDelete(entry: GlucoseEntry)
}