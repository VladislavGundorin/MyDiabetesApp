package com.example.mydiabetesapp.feature.hba1c.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cRepository

class Hba1cViewModelFactory(private val repo: Hba1cRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        Hba1cViewModel(repo) as T
}