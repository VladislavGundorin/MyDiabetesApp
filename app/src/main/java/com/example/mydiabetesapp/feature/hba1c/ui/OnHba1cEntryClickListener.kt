package com.example.mydiabetesapp.feature.hba1c.ui

import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry

interface OnHba1cEntryClickListener {
    fun onEdit(entry: Hba1cEntry)
    fun onDelete(entry: Hba1cEntry)
}
