package com.example.mydiabetesapp.feature.export.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mydiabetesapp.feature.export.data.ReportRepository

class ReportVMFactory(private val repo: ReportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(cls: Class<T>): T =
        ReportViewModel(repo) as T
}