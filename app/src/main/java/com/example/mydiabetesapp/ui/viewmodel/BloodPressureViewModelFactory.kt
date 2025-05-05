package com.example.mydiabetesapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mydiabetesapp.repository.BloodPressureRepository

class BloodPressureViewModelFactory(
    private val repo: BloodPressureRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BloodPressureViewModel(repo) as T
}
