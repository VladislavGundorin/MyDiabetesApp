package com.example.mydiabetesapp.feature.weight.ui

import com.example.mydiabetesapp.feature.weight.data.WeightEntry

interface OnWeightEntryClickListener {
    fun onEdit(entry: WeightEntry)
    fun onDelete(entry: WeightEntry)
}