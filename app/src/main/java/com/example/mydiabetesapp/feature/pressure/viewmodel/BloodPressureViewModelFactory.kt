package com.example.mydiabetesapp.feature.pressure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureRepository

class BloodPressureViewModelFactory(
    private val repo: BloodPressureRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BloodPressureViewModel(repo) as T
}
