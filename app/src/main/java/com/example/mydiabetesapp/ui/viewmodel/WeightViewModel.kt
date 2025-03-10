package com.example.mydiabetesapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.data.database.WeightEntry
import com.example.mydiabetesapp.repository.WeightRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeightViewModel(private val repository: WeightRepository) : ViewModel() {

    val entries: StateFlow<List<WeightEntry>> = repository.getWeightEntries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addEntry(entry: WeightEntry) {
        viewModelScope.launch {
            repository.addWeightEntry(entry)
        }
    }

    fun updateEntry(entry: WeightEntry) {
        viewModelScope.launch {
            repository.updateWeightEntry(entry)
        }
    }

    fun deleteEntry(entry: WeightEntry) {
        viewModelScope.launch {
            repository.deleteWeightEntry(entry)
        }
    }
}
