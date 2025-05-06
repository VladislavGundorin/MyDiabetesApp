package com.example.mydiabetesapp.feature.export.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.feature.export.data.ReportEntry
import com.example.mydiabetesapp.feature.export.data.ReportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReportViewModel(private val repo: ReportRepository) : ViewModel() {

    val reports: StateFlow<List<ReportEntry>> =
        repo.reports().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun generate(ctx: Context, start: LocalDate, end: LocalDate) = viewModelScope.launch {
        repo.createReport(ctx, start, end)
    }

    fun delete(report: ReportEntry) = viewModelScope.launch {
        repo.delete(report)
    }
}
