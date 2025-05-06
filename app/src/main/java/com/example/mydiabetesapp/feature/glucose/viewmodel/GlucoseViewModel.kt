package com.example.mydiabetesapp.feature.glucose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.feature.glucose.data.GlucoseEntry
import com.example.mydiabetesapp.feature.glucose.data.GlucoseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GlucoseViewModel(private val repository: GlucoseRepository) : ViewModel() {

    private val _entries = MutableStateFlow<List<GlucoseEntry>>(emptyList())
    val entries: StateFlow<List<GlucoseEntry>> = _entries

    init {
        viewModelScope.launch {
            repository.getAllEntries().collect { list ->
                _entries.value = list
            }
        }
    }

    fun addEntry(entry: GlucoseEntry) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }

    fun deleteEntry(entry: GlucoseEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun updateEntry(entry: GlucoseEntry) {
        viewModelScope.launch {
            repository.update(entry)
        }
    }
}
