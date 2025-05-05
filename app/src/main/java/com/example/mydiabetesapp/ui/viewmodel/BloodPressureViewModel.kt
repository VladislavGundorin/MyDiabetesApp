package com.example.mydiabetesapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.data.database.BloodPressureEntry
import com.example.mydiabetesapp.repository.BloodPressureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BloodPressureViewModel(private val repository: BloodPressureRepository) : ViewModel() {

    private val _entries = MutableStateFlow<List<BloodPressureEntry>>(emptyList())
    val entries: StateFlow<List<BloodPressureEntry>> = _entries

    init {
        viewModelScope.launch {
            repository.getAll().collect { _entries.value = it }
        }
    }

    fun insert(entry: BloodPressureEntry) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }

    fun update(entry: BloodPressureEntry) {
        viewModelScope.launch {
            repository.update(entry)
        }
    }

    fun delete(entry: BloodPressureEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }
}
