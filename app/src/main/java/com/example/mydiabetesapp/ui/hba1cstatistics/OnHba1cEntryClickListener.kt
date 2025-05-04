package com.example.mydiabetesapp.ui.hba1cstatistics

import com.example.mydiabetesapp.data.database.Hba1cEntry

interface OnHba1cEntryClickListener {
    fun onEdit(entry: Hba1cEntry)
    fun onDelete(entry: Hba1cEntry)
}
