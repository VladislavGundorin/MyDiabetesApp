package com.example.mydiabetesapp.feature.glucose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mydiabetesapp.feature.glucose.data.GlucoseRepository

class GlucoseViewModelFactory(private val repository: GlucoseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlucoseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GlucoseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
