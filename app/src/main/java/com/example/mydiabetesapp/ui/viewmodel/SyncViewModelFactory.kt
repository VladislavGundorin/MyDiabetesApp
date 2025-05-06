package com.example.mydiabetesapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mydiabetesapp.repository.GlucoseRepository

class SyncViewModelFactory(private val repo: GlucoseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return SyncViewModel(repo) as T
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
